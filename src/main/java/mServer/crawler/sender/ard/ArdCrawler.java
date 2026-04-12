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
  private static final int MAX_DAYS_PAST = 7;
  private static final int MAX_DAYS_FUTURE = 7;
  private static final int MAX_DAYS_PAST_AVAILABLE = 7;
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static final String[] MISSING_TOPIC_IDS = new String[]{
  };

  public ArdCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);
  }

  @Override
  protected synchronized void meldungThreadUndFertig() {
    // der MediathekReader ist erst fertig wenn nur noch ein Thread läuft
    // dann zusätzliche Sender, die der Crawler bearbeitet, beenden
    if (getThreads() <= 1) {
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.RBB);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.SWR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.MDR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.NDR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.WDR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.HR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.BR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.RBTV);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.ONE);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.ARD_ALPHA);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + "Funk.net");
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.TAGESSCHAU24);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.SR);
      mlibFilmeSuchen.meldenFertig(getRunIdentifierBase() + "-" + Const.PHOENIX);
    }

    super.meldungThreadUndFertig();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < MAX_DAYS_PAST; i++) {
      addDayUrls(dayUrlsToCrawl, now.minusDays(i));
    }

    if (CrawlerTool.loadLongMax()) {
      for (int i = 0; i < MAX_DAYS_FUTURE; i++) {
        addDayUrls(dayUrlsToCrawl, now.plusDays(i));
      }
    }

    addSpecialDays(dayUrlsToCrawl);

    return dayUrlsToCrawl;
  }

  private void addDayUrls(ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl, LocalDateTime day) {
    final String formattedDay = day.format(DAY_PAGE_DATE_FORMATTER);
    for (String client : ArdConstants.CLIENTS_DAY) {
      final String url = String.format(ArdConstants.DAY_PAGE_URL, formattedDay, client);
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

    final ArdTopicGroupsTask groupsToAsset = new ArdTopicGroupsTask(this, new ConcurrentLinkedQueue<>(topics));
    final Set<CrawlerUrlDTO> assitUrls = new HashSet<>();
    assitUrls.addAll(forkJoinPool.submit(groupsToAsset).get());
    Log.sysLog("ard sender group assit tasks: " + assitUrls.size());
    addAdditionalTopics(assitUrls);
    Log.sysLog("ard mediathek assit tasks with additional: " + assitUrls.size());

    ConcurrentLinkedQueue<CrawlerUrlDTO> topicUrls = new ConcurrentLinkedQueue<>(assitUrls);

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
