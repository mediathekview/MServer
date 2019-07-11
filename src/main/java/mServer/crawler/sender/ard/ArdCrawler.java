package mServer.crawler.sender.ard;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.ard.tasks.ArdDayPageTask;
import mServer.crawler.sender.ard.tasks.ArdFilmDetailTask;
import mServer.crawler.sender.ard.tasks.ArdTopicPageTask;
import mServer.crawler.sender.ard.tasks.ArdTopicsOverviewTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;

public class ArdCrawler extends MediathekCrawler {
  private static final int MAX_DAYS_PAST = 2;

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
    }

    super.meldungThreadUndFertig();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    final LocalDateTime now = LocalDateTime.now();
    for (int i = 0; i < MAX_DAYS_PAST; i++) {
      final String url
              = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
                      .addSearchDate(now.minusDays(i))
                      .addSavedQuery(
                              ArdConstants.QUERY_DAY_SEARCH_VERSION, ArdConstants.QUERY_DAY_SEARCH_HASH)
                      .build();

      dayUrlsToCrawl.offer(new CrawlerUrlDTO(url));
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
    final ArdTopicsOverviewTask topicsTask
            = new ArdTopicsOverviewTask(this, createTopicsOverviewUrl());

    final ConcurrentLinkedQueue<CrawlerUrlDTO> topicUrls
            = new ConcurrentLinkedQueue<>(forkJoinPool.submit(topicsTask).get());

    final ArdTopicPageTask topicTask = new ArdTopicPageTask(this, topicUrls);
    final Set<ArdFilmInfoDto> filmInfos = forkJoinPool.submit(topicTask).get();
    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url
            = new ArdUrlBuilder(ArdConstants.BASE_URL, ArdConstants.DEFAULT_CLIENT)
                    .addSavedQuery(ArdConstants.QUERY_TOPICS_VERSION, ArdConstants.QUERY_TOPICS_HASH)
                    .build();

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }
}
