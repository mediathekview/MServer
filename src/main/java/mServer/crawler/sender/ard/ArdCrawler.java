package mServer.crawler.sender.ard;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.ard.tasks.ArdDayPageTask;
import mServer.crawler.sender.ard.tasks.ArdFilmDetailTask;
import mServer.crawler.sender.ard.tasks.ArdTopicPageTask;
import mServer.crawler.sender.ard.tasks.ArdTopicsOverviewTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class ArdCrawler extends MediathekCrawler {

  private static final int MAX_DAYS_PAST = 2;
  private static final DateTimeFormatter DAY_PAGE_DATE_FORMATTER
          = DateTimeFormatter.ofPattern("yyyy-MM-dd");

  public static final String SENDERNAME = Const.ARD;

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
      mlibFilmeSuchen.meldenFertig("rbtv");
    }

    super.meldungThreadUndFertig();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < MAX_DAYS_PAST; i++) {
      final String day = now.minusDays(i).format(DAY_PAGE_DATE_FORMATTER);

      for (String client : ArdConstants.CLIENTS) {
        final String url = String.format(ArdConstants.DAY_PAGE_URL, client, day, day, ArdConstants.DAY_PAGE_SIZE);
        dayUrlsToCrawl.offer(new CrawlerUrlDTO(url));
      }
    }
    return dayUrlsToCrawl;
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    final ConcurrentLinkedQueue<ArdFilmInfoDto> shows = new ConcurrentLinkedQueue<>();
    try {

      if (CrawlerTool.loadLongMax()) {
        shows.addAll(getTopicsEntries());
      }

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

    ConcurrentLinkedQueue<CrawlerUrlDTO> topicUrls = new ConcurrentLinkedQueue<>(topics);

    final ArdTopicPageTask topicTask = new ArdTopicPageTask(this, topicUrls);
    final Set<ArdFilmInfoDto> filmInfos = forkJoinPool.submit(topicTask).get();
    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getTopicEntriesBySender(final String sender) throws ExecutionException, InterruptedException {
    ArdTopicsOverviewTask topicsTask
            = new ArdTopicsOverviewTask(this, createTopicsOverviewUrl(sender));

    ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>(forkJoinPool.submit(topicsTask).get());
    Log.sysLog(sender + " topic entries: " + queue.size());
    return queue;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl(final String client) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url = String.format(ArdConstants.TOPICS_URL, client);

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }
}
