package mServer.crawler.sender.ard;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.ard.tasks.ArdDayPageTask;
import mServer.crawler.sender.ard.tasks.ArdFilmDetailTask;
import mServer.crawler.sender.ard.tasks.ArdTopicPageTask;
import mServer.crawler.sender.ard.tasks.ArdTopicsOverviewTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArdCrawler extends MediathekReader {

  private static final Logger LOG = LogManager.getLogger(ArdCrawler.class);

  public static final String SENDERNAME = Const.ARD;

  private final ForkJoinPool forkJoinPool;

  public ArdCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, SENDERNAME, 0, 1, startPrio);

    forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors() * 4);
  }

  @Override
  protected void addToList() {
    meldungStart();

    try {
      RecursiveTask<Set<DatenFilm>> filmTask = createCrawlerTask();
      if (filmTask != null) {
        Set<DatenFilm> films = forkJoinPool.invoke(filmTask);

        Log.sysLog("ARD Filme einsortieren...");

        films.forEach(film -> {
          if (!Config.getStop()) {
            addFilm(film);
          }
        });

        Log.sysLog("ARD Film einsortieren fertig");
      }
    } finally {
      //explicitely shutdown the pool
      shutdownAndAwaitTermination(forkJoinPool, 60, TimeUnit.SECONDS);
    }

    Log.sysLog("ARD fertig");

    meldungThreadUndFertig();
  }

  void shutdownAndAwaitTermination(ExecutorService pool, long delay, TimeUnit delayUnit) {
    pool.shutdown();
    Log.sysLog("ARD shutdown pool...");
    try {
      if (!pool.awaitTermination(delay, delayUnit)) {
        pool.shutdownNow();
        if (!pool.awaitTermination(delay, delayUnit)) {
          Log.sysLog("ARD: Pool nicht beendet");
        }
      }
    } catch (InterruptedException ie) {
      pool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    LocalDateTime now = LocalDateTime.now();
    for (int i = 0;
            i <= 7; i++) {
      final String url = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
              .addSearchDate(now.minusDays(i))
              .addSavedQuery(ArdConstants.QUERY_DAY_SEARCH_VERSION, ArdConstants.QUERY_DAY_SEARCH_HASH)
              .build();

      dayUrlsToCrawl.offer(new CrawlerUrlDTO(url));
    }
    return dayUrlsToCrawl;

  }

  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    try {
      ConcurrentLinkedQueue<ArdFilmInfoDto> shows = new ConcurrentLinkedQueue<>();
      shows.addAll(getDaysEntries());

      if (CrawlerTool.loadLongMax()) {
        getTopicsEntries().forEach(show -> {
          if (!shows.contains(show)) {
            shows.add(show);
          }
        });
      }

      Log.sysLog("ARD Anzahl: " + shows.size());
      meldungAddMax(shows.size());

      return new ArdFilmDetailTask(this, shows);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ARD crawler.", ex);
    }
    return null;
  }

  private Set<ArdFilmInfoDto> getDaysEntries() throws InterruptedException, ExecutionException {
    ArdDayPageTask dayTask = new ArdDayPageTask(this, createDayUrlsToCrawl());
    Set<ArdFilmInfoDto> shows = forkJoinPool.submit(dayTask).get();

    Log.sysLog("ARD Sendung verpasst? Anzahl: " + shows.size());

    return shows;
  }

  private Set<ArdFilmInfoDto> getTopicsEntries() throws ExecutionException, InterruptedException {
    ArdTopicsOverviewTask topicsTask = new ArdTopicsOverviewTask(this, createTopicsOverviewUrl());

    ConcurrentLinkedQueue topicUrls = new ConcurrentLinkedQueue();
    topicUrls.addAll(forkJoinPool.submit(topicsTask).get());

    ArdTopicPageTask topicTask = new ArdTopicPageTask(this, topicUrls);
    Set<ArdFilmInfoDto> filmInfos = forkJoinPool.submit(topicTask).get();

    Log.sysLog("ARD Sendung A-Z Anzahl: " + filmInfos.size());

    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    String url = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
            .addSavedQuery(ArdConstants.QUERY_TOPICS_VERSION, ArdConstants.QUERY_TOPICS_HASH)
            .build();

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }
}
