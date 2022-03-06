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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
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

  private final Map<String, ArteLanguage> senderLanguages = new HashMap<>();

  public static final String ARTE_EN = "ARTE.EN";
  public static final String ARTE_ES = "ARTE.ES";
  public static final String ARTE_IT = "ARTE.IT";
  public static final String ARTE_PL = "ARTE.PL";

  public ArteCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.ARTE_DE, 0, 1, startPrio);
  }

  @Override
  protected synchronized void meldungStart() {
    super.meldungStart();

    senderLanguages.put(Const.ARTE_DE, ArteLanguage.DE);
    senderLanguages.put(Const.ARTE_FR, ArteLanguage.FR);
    if (LocalDate.now().getDayOfYear() % 2 == 0) {
      senderLanguages.put(ARTE_EN, ArteLanguage.EN);
      senderLanguages.put(ARTE_ES, ArteLanguage.ES);
    } else {
      senderLanguages.put(ARTE_IT, ArteLanguage.IT);
      senderLanguages.put(ARTE_PL, ArteLanguage.PL);
    }

    // starte Sprachen Sender, da es sonst zu doppelten Sendern kommen kann
    senderLanguages.keySet().forEach(sender -> mlibFilmeSuchen.melden(sender, getMax(), getProgress(), ""));
  }

  @Override
  protected synchronized void meldungThreadUndFertig() {
    // der MediathekReader ist erst fertig wenn nur noch ein Thread läuft
    // dann zusätzliche Sender, die der Crawler bearbeitet, beenden
    if (getThreads() <= 1) {
      senderLanguages.keySet().stream()
              // DE nicht beenden, das erfolgt durch den Aufruf der Basisklasse
              .filter(sender -> !sender.equals(Const.ARTE_DE))
              .forEach(sender -> mlibFilmeSuchen.meldenFertig(sender));
    }

    super.meldungThreadUndFertig();
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> generateSendungVerpasstUrls(ArteLanguage language) {

    int maximumDaysPast = CrawlerTool.loadLongMax() ? MAXIMUM_DAYS_PAST_LONG : MAXIMUM_DAYS_PAST_SHORT;
    int maximumDaysFuture = CrawlerTool.loadLongMax() ? MAXIMUM_DAYS_FUTURE_LONG : MAXIMUM_DAYS_FUTURE_SHORT;

    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungVerpasstUrls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < (maximumDaysPast + maximumDaysFuture); i++) {
      sendungVerpasstUrls.add(
              new CrawlerUrlDTO(
                      String.format(
                              ArteConstants.DAY_PAGE_URL,
                              language.getLanguageCode().toLowerCase(),
                              LocalDateTime.now()
                                      .plus(
                                              maximumDaysFuture,
                                              ChronoUnit.DAYS)
                                      .minus(i, ChronoUnit.DAYS)
                                      .format(SENDUNG_VERPASST_DATEFORMATTER))));
    }
    return sendungVerpasstUrls;
  }

  private Set<ArteFilmUrlDto> getCategoriesEntries(ArteLanguage language)
          throws ExecutionException, InterruptedException {
    final ArteSubcategoriesTask subcategoriesTask =
            new ArteSubcategoriesTask(this, createTopicsOverviewUrl(language));

    final ConcurrentLinkedQueue<TopicUrlDTO> subcategoriesUrl = new ConcurrentLinkedQueue<>();
    subcategoriesUrl.addAll(forkJoinPool.submit(subcategoriesTask).get());

    final ArteSubcategoryVideosTask subcategoryVideosTask =
            new ArteSubcategoryVideosTask(
                    this, subcategoriesUrl, ArteConstants.BASE_URL_WWW, language);
    final Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(subcategoryVideosTask).get();

    Log.sysLog("ARTE: Anzahl Kategorie: " + filmInfos.size());

    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createTopicsOverviewUrl(ArteLanguage language) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url =
            String.format(ArteConstants.URL_SUBCATEGORIES, language.getLanguageCode().toLowerCase());

    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }

  private Set<ArteFilmUrlDto> getVideoListVideos(ArteLanguage language, String videoListType)
          throws ExecutionException, InterruptedException {
    final ArteAllVideosTask videosTask =
            new ArteAllVideosTask(this, createVideoListUrls(language, videoListType), language);
    final Set<ArteFilmUrlDto> filmInfos = forkJoinPool.submit(videosTask).get();

    Log.sysLog("ARTE: Anzahl VideoList " + videoListType + ": " + filmInfos.size());

    return filmInfos;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createVideoListUrls(ArteLanguage language, String videoListType) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    for (int i = 1; i <= getVideoListMaximumSubpages(videoListType); i++) {
      final String url =
              String.format(
                      ArteConstants.URL_VIDEO_LIST,
                      ArteConstants.BASE_URL_WWW,
                      language.getLanguageCode().toLowerCase(),
                      videoListType,
                      i);

      urls.add(new CrawlerUrlDTO(url));
    }
    return urls;
  }

  private int getVideoListMaximumSubpages(String videoListType) {
    if (CrawlerTool.loadLongMax()) {
      if (Objects.equals(videoListType, ArteConstants.VIDEO_LIST_TYPE_RECENT)) {
        return VIDEO_LIST_SUBPAGES_LONG;
      }
      return VIDEO_LIST_SUBPAGES_SHORT;
    }
    return VIDEO_LIST_SUBPAGES_SHORT;
  }

  private Set<ArteFilmUrlDto> getDaysEntries(ArteLanguage language) throws InterruptedException, ExecutionException {

    final ArteDayPageTask dayTask =
            new ArteDayPageTask(this, generateSendungVerpasstUrls(language), language);
    final Set<ArteFilmUrlDto> shows = forkJoinPool.submit(dayTask).get();

    Log.sysLog("ARTE: Anzahl Tage: " + shows.size());

    return shows;
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    final Set<ArteFilmUrlDto> shows = new HashSet<>();

    senderLanguages.forEach((sender, language) -> {
      try {
        if (isDayEntriesEnabled(sender)) {
          getDaysEntries(language).forEach(show -> addShow(shows, sender, show));
        }
        getVideoListVideos(language, ArteConstants.VIDEO_LIST_TYPE_RECENT).forEach(show -> addShow(shows, sender, show));

        if (CrawlerTool.loadLongMax()) {
          getCategoriesEntries(language).forEach(show -> addShow(shows, sender, show));

          getVideoListVideos(language, ArteConstants.VIDEO_LIST_TYPE_LAST_CHANCE)
                  .forEach(show -> addShow(shows, sender, show));
        }
      } catch (final InterruptedException | ExecutionException ex) {
        LOG.fatal("Exception in ARTE crawler.", ex);
        Thread.currentThread().interrupt();
      }
    });

    Log.sysLog("ARTE: Anzahl: " + shows.size());
    meldungAddMax(shows.size());

    return new ArteFilmTask(
            this, new ConcurrentLinkedQueue<>(shows), LocalDateTime.now());
  }

  private boolean isDayEntriesEnabled(String sender) {
    return Const.ARTE_DE.equals(sender) || Const.ARTE_FR.equals(sender);
  }

  private void addShow(Set<ArteFilmUrlDto> shows, String sender, ArteFilmUrlDto show) {
    show.setSender(sender);
    shows.add(show);
  }
}
