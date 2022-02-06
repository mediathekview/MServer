package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.arte.tasks.*;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.TopicUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class ArteCrawler extends MediathekCrawler {

  private static final Logger LOG = LogManager.getLogger(ArteCrawler.class);
  private static final DateTimeFormatter SENDUNG_VERPASST_DATEFORMATTER =
          DateTimeFormatter.ofPattern("yyyy-MM-dd");

  private static final int MAXIMUM_DAYS_PAST_LONG = 7;
  private static final int MAXIMUM_DAYS_FUTURE_LONG = 14;
  private static final int VIDEO_LIST_SUBPAGES_LONG = 5;
  private static final int VIDEO_LIST_SUBPAGES_SHORT = 1;
  private static final int MAXIMUM_DAYS_PAST_SHORT = 3;
  private static final int MAXIMUM_DAYS_FUTURE_SHORT = 3;

  public ArteCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.ARTE_DE, 0, 1, startPrio);
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> generateSendungVerpasstUrls() {

    int maximumDaysPast = CrawlerTool.loadLongMax() ? MAXIMUM_DAYS_PAST_LONG : MAXIMUM_DAYS_PAST_SHORT;
    int maximumDaysFuture = CrawlerTool.loadLongMax() ? MAXIMUM_DAYS_FUTURE_LONG : MAXIMUM_DAYS_FUTURE_SHORT;

    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungVerpasstUrls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < (maximumDaysPast + maximumDaysFuture); i++) {
      sendungVerpasstUrls.add(
              new CrawlerUrlDTO(
                      String.format(
                              ArteConstants.DAY_PAGE_URL,
                              getLanguage().getLanguageCode().toLowerCase(),
                              LocalDateTime.now()
                                      .plus(
                                              maximumDaysFuture,
                                              ChronoUnit.DAYS)
                                      .minus(i, ChronoUnit.DAYS)
                                      .format(SENDUNG_VERPASST_DATEFORMATTER))));
    }
    return sendungVerpasstUrls;
  }

  private Set<ArteFilmUrlDto> getCategoriesEntries()
          throws ExecutionException, InterruptedException {
    final ArteSubcategoriesTask subcategoriesTask =
            new ArteSubcategoriesTask(this, createTopicsOverviewUrl());

    final ConcurrentLinkedQueue<TopicUrlDTO> subcategoriesUrl = new ConcurrentLinkedQueue<>();
    subcategoriesUrl.addAll(forkJoinPool.submit(subcategoriesTask).get());

    final ArteSubcategoryVideosTask subcategoryVideosTask =
            new ArteSubcategoryVideosTask(
                    this, subcategoriesUrl, ArteConstants.BASE_URL_WWW, getLanguage());
    final Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(subcategoryVideosTask).get();

    Log.sysLog("ARTE: Anzahl Kategorie: " + filmInfos.size());

    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url =
            String.format(ArteConstants.URL_SUBCATEGORIES, getLanguage().toString().toLowerCase());

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }

  private Set<ArteFilmUrlDto> getVideoListVideos(String videoListType)
          throws ExecutionException, InterruptedException {
    final ArteAllVideosTask videosTask =
            new ArteAllVideosTask(this, createVideoListUrls(videoListType), getLanguage());
    final Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(videosTask).get();

    Log.sysLog("ARTE: Anzahl VideoList " + videoListType + ": " + filmInfos.size());

    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createVideoListUrls(String videoListType) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    for (int i = 1; i <= getVideoListMaximumSubpages(); i++) {
      final String url =
              String.format(
                      ArteConstants.URL_VIDEO_LIST,
                      ArteConstants.BASE_URL_WWW,
                      getLanguage().toString().toLowerCase(),
                      videoListType,
                      i);

      urls.add(new CrawlerUrlDTO(url));
    }
    return urls;
  }

  private int getVideoListMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return VIDEO_LIST_SUBPAGES_LONG;
    }
    return VIDEO_LIST_SUBPAGES_SHORT;
  }

  private Set<ArteFilmUrlDto> getDaysEntries() throws InterruptedException, ExecutionException {

    final ArteDayPageTask dayTask =
            new ArteDayPageTask(this, generateSendungVerpasstUrls(), getLanguage());
    final Set<ArteFilmUrlDto> shows = forkJoinPool.submit(dayTask).get();

    Log.sysLog("ARTE: Anzahl Tage: " + shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    try {
      final Set<ArteFilmUrlDto> shows = new HashSet<>();
      if (isDayEntriesEnabled()) {
        shows.addAll(getDaysEntries());
      }
      getVideoListVideos(ArteConstants.VIDEO_LIST_TYPE_RECENT).forEach(shows::add);

      if (CrawlerTool.loadLongMax()) {
        getCategoriesEntries().forEach(shows::add);

        getVideoListVideos(ArteConstants.VIDEO_LIST_TYPE_LAST_CHANCE)
                .forEach(shows::add);
      }

      Log.sysLog("ARTE: Anzahl: " + shows.size());
      meldungAddMax(shows.size());

      return new ArteFilmTask(
              this, new ConcurrentLinkedQueue<>(shows), getSendername(), LocalDateTime.now());
    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ARTE crawler.", ex);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  protected boolean isDayEntriesEnabled() {
    return true;
  }

  protected ArteLanguage getLanguage() {
    return ArteLanguage.DE;
  }
}
