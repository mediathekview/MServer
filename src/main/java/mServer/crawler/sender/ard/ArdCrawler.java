package mServer.crawler.sender.ard;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.ard.tasks.*;
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class ArdCrawler extends MediathekCrawler {

  public static final String SENDERNAME = Const.ARD;
  private static final int MAX_DAYS_PAST = 2;
  private static final int MAX_DAYS_PAST_AVAILABLE = 6;
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static final String[] MISSING_TOPIC_IDS = new String[]{
          // Dahoam is dahoam
          "Y3JpZDovL2JyLmRlL2Jyb2FkY2FzdFNlcmllcy9icm9hZGNhc3RTZXJpZXM6L2JyZGUvZmVybnNlaGVuL2JheWVyaXNjaGVzLWZlcm5zZWhlbi9zZW5kdW5nZW4vZGFob2FtLWlzLWRhaG9hbQ",
          // Rote Rosen
          "Y3JpZDovL3dkci5kZS9vbmUvcm90ZXJvc2Vu",
          // Sturm der Liebe
          "Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZQ",
          // in aller freundschaft -die jungen ärzte
          "Y3JpZDovL21kci5kZS9zZW5kZXJlaWhlbi9zdGFmZmVsc2VyaWUtaW4tYWxsZXItZnJldW5kc2NoYWZ0LWRpZS1qdW5nZW4tYWVyenRl"
  };

  public ArdCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);
  }

  @Override
  protected synchronized void meldungThreadUndFertig() {
    // der MediathekReader ist erst fertig wenn nur noch ein Thread läuft
    // dann zusätzliche Sender, die der Crawler bearbeitet, beenden
    if (getThreads() <= 1) {
      mlibFilmeSuchen.meldenFertig(Const.RBB);
      mlibFilmeSuchen.meldenFertig(Const.SWR);
      mlibFilmeSuchen.meldenFertig(Const.MDR);
      mlibFilmeSuchen.meldenFertig(Const.NDR);
      mlibFilmeSuchen.meldenFertig(Const.WDR);
      mlibFilmeSuchen.meldenFertig(Const.HR);
      mlibFilmeSuchen.meldenFertig(Const.BR);
      mlibFilmeSuchen.meldenFertig("rbtv");
      mlibFilmeSuchen.meldenFertig("ONE");
      mlibFilmeSuchen.meldenFertig("ARD-alpha");
      mlibFilmeSuchen.meldenFertig("Funk.net");
      mlibFilmeSuchen.meldenFertig(Const.SR);
      mlibFilmeSuchen.meldenFertig(Const.PHOENIX);
    }

    super.meldungThreadUndFertig();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < MAX_DAYS_PAST; i++) {
      addDayUrls(dayUrlsToCrawl, now.minusDays(i));
    }

    addSpecialDays(dayUrlsToCrawl);

    return dayUrlsToCrawl;
  }

  private void addDayUrls(ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl, LocalDateTime day) {
    final String formattedDay = day.format(DAY_PAGE_DATE_FORMATTER);
    for (String client : ArdConstants.CLIENTS) {
      final String url = String.format(ArdConstants.DAY_PAGE_URL, client, formattedDay, formattedDay, ArdConstants.DAY_PAGE_SIZE);
      dayUrlsToCrawl.offer(new CrawlerUrlDTO(url));
    }
  }

  private void addSpecialDays(
          ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl) {
    final LocalDateTime[] specialDates = new LocalDateTime[]{
    };

    final LocalDateTime minDayOnline = LocalDateTime.now().minusDays(MAX_DAYS_PAST_AVAILABLE);

    for (LocalDateTime specialDate : specialDates) {
      if (specialDate.isAfter(minDayOnline)) {
        addDayUrls(dayUrlsToCrawl, specialDate);
      }
    }
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    final ConcurrentLinkedQueue<ArdFilmInfoDto> shows = new ConcurrentLinkedQueue<>();
    try {

      if (CrawlerTool.loadLongMax()) {
        shows.addAll(getTopicsEntries());
      }
      Log.sysLog("ARD Anzahl topics: " + shows.size());
      getDaysEntries().forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });

    } catch (InterruptedException | ExecutionException exception) {
      Log.errorLog(56146546, exception);
    }
    Log.sysLog("ARD Anzahl: " + shows.size());

    meldungAddMax(shows.size());

    return new ArdFilmDetailTask(this, shows);
  }

  private Set<ArdFilmInfoDto> getDaysEntries() throws InterruptedException, ExecutionException {
    final ArdDayPageTask dayTask = new ArdDayPageTask(this, createDayUrlsToCrawl());
    final Set<ArdFilmInfoDto> shows = forkJoinPool.submit(dayTask).get();
    return shows;
  }

  private Set<ArdFilmInfoDto> getTopicsEntries() throws ExecutionException, InterruptedException {
    Set<CrawlerUrlDTO> topics = new HashSet<>();
    topics.addAll(getTopicEntriesBySender(ArdConstants.DEFAULT_CLIENT));
    for (String client : ArdConstants.CLIENTS) {
      topics.addAll(getTopicEntriesBySender(client));
    }

    Log.sysLog("ard mediathek topics: " + topics.size());
    addAdditionalTopics(topics);
    Log.sysLog("ard mediathek topics with additional: " + topics.size());
    ConcurrentLinkedQueue<CrawlerUrlDTO> topicUrls = new ConcurrentLinkedQueue<>(topics);

    final ArdTopicPageTask topicTask = new ArdTopicPageTask(this, topicUrls);
    final Set<ArdFilmInfoDto> filmInfos = forkJoinPool.submit(topicTask).get();
    Log.sysLog("ard shows by topics: " + filmInfos.size());
    return filmInfos;
  }

  // temporary workaround for missing topics
  private void addAdditionalTopics(Set<CrawlerUrlDTO> topics) {
    for (String topicId : MISSING_TOPIC_IDS) {
      topics.add(new CrawlerUrlDTO(String.format(ArdConstants.TOPIC_URL, topicId, ArdConstants.TOPIC_PAGE_SIZE)));
    }
  }

  private Set<CrawlerUrlDTO> getTopicEntriesBySender(final String sender) throws ExecutionException, InterruptedException {
    ArdTopicsTask topicsTask
            = new ArdTopicsTask(this, sender, createTopicsOverviewUrl(sender));

    ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>(forkJoinPool.submit(topicsTask).get());
    Log.sysLog(sender + " topics task entries: " + queue.size());

    final Set<CrawlerUrlDTO> topicUrls = forkJoinPool.submit(new ArdTopicsLetterTask(this, sender, queue)).get();
    Log.sysLog(sender + " topics: " + topicUrls.size());
    return topicUrls;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl(final String client) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url = String.format(ArdConstants.TOPICS_URL, client);

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }
}
