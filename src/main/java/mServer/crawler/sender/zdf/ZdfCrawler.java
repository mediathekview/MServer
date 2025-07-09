package mServer.crawler.sender.zdf;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.zdf.tasks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class ZdfCrawler extends MediathekCrawler {

  private static final Logger LOG = LogManager.getLogger(ZdfCrawler.class);
  private static final int MAX_LETTER_PAGEGS = 27;

  private static final String AUTH_KEY = "aa3noh4ohz9eeboo8shiesheec9ciequ9Quah7el";

  JsoupConnection jsoupConnection = new JsoupConnection();

  public ZdfCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, Const.ZDF, 0, 1, startPrio);
  }


  @Override
  protected synchronized void meldungThreadUndFertig() {
    // der MediathekReader ist erst fertig wenn nur noch ein Thread läuft
    // dann zusätzliche Sender, die der Crawler bearbeitet, beenden
    if (getThreads() <= 1) {
      mlibFilmeSuchen.meldenFertig(Const.ZDF_TIVI);
      mlibFilmeSuchen.meldenFertig(Const.ZDF_INFO);
      mlibFilmeSuchen.meldenFertig(Const.ZDF_NEO);
    }

    super.meldungThreadUndFertig();
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    try {
      if (CrawlerTool.loadLongMax()) {
        Set<ZdfFilmDto> shows = new HashSet<>();
        shows.addAll(getTopicsEntries());

        Log.sysLog(getSendername() + " Anzahl: " + shows.size());
        meldungAddMax(shows.size());

        return new ZdfFilmTask(this, new ConcurrentLinkedQueue<>(shows), AUTH_KEY);
      } else {
        final ZdfConfiguration configuration = loadConfiguration();
        if (configuration.getSearchAuthKey().isPresent() && configuration.getVideoAuthKey().isPresent()) {
          Set<CrawlerUrlDTO> shows = new HashSet<>(getDaysEntries(configuration));
          Log.sysLog(getSendername() + " Anzahl: " + shows.size());
          meldungAddMax(shows.size());
          return new ZdfFilmDetailTask(this, getApiUrlBase(), new ConcurrentLinkedQueue<>(shows), configuration.getVideoAuthKey(), ZdfConstants.PARTNER_TO_SENDER);
        }
      }
    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSendername(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSendername(), ex);
    }
    return null;
  }

  private Queue<ZdfFilmDto> getTopicsEntries() throws ExecutionException, InterruptedException {

    final ConcurrentLinkedQueue<ZdfFilmDto> shows = new ConcurrentLinkedQueue<>();

    ZdfLetterPageTask letterPageTask =
            new ZdfLetterPageTask(this, createLetterPageUrls(), AUTH_KEY);
    final Set<ZdfTopicUrlDto> topicUrls = forkJoinPool.submit(letterPageTask).get();

    Log.sysLog("ZDF: letter topics: " + topicUrls.size());

    if (Config.getStop()) {
      return shows;
    }

    final ZdfPubFormTask pubFormTask = new ZdfPubFormTask(this, createPubFormUrls(), AUTH_KEY);
    final Set<ZdfPubFormResult> pubFormUrls = forkJoinPool.submit(pubFormTask).get();

    Log.sysLog("ZDF: Pubform urls: " + pubFormUrls.size());

    if (Config.getStop()) {
      return shows;
    }

    pubFormUrls.forEach(
            pubFormResult -> {
              topicUrls.addAll(pubFormResult.getTopics().getElements());
              shows.addAll(pubFormResult.getFilms());
            });

    Log.sysLog("ZDF: Pubform topics: " + pubFormUrls.size());

    if (Config.getStop()) {
      return shows;
    }

    ZdfTopicSeasonTask topicSeasonTask =
            new ZdfTopicSeasonTask(this, new ConcurrentLinkedQueue<>(topicUrls), AUTH_KEY);
    final Set<ZdfFilmDto> zdfFilmDtos = forkJoinPool.submit(topicSeasonTask).get();
    shows.addAll(zdfFilmDtos);

    return shows;
  }

  private ConcurrentLinkedQueue<ZdfPubFormDto> createPubFormUrls() {
    ConcurrentLinkedQueue<ZdfPubFormDto> urls = new ConcurrentLinkedQueue<>();
    ZdfConstants.SPECIAL_COLLECTION_IDS.forEach((collectionId, topic) -> {
      final String url =
              ZdfUrlBuilder.buildTopicNoSeasonUrl(
                      ZdfConstants.EPISODES_PAGE_SIZE, collectionId, ZdfConstants.NO_CURSOR);
      urls.add(new ZdfPubFormDto(topic, collectionId, url));
    });
    return urls;
  }

  private ConcurrentLinkedQueue<ZdfLetterDto> createLetterPageUrls() {
    final ConcurrentLinkedQueue<ZdfLetterDto> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < MAX_LETTER_PAGEGS; i++) {
      urls.add(new ZdfLetterDto(i, ZdfUrlBuilder.buildLetterPageUrl(ZdfConstants.NO_CURSOR, i)));
    }

    return urls;
  }

  private ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfIndexPageTask task = new ZdfIndexPageTask(this, getUrlBase(), jsoupConnection);
    return forkJoinPool.submit(task).get();
  }

  private Set<CrawlerUrlDTO> getDaysEntries(ZdfConfiguration configuration)
          throws InterruptedException, ExecutionException {
    final ZdfDayPageTask dayTask
            = new ZdfDayPageTask(this, getApiUrlBase(), getDayUrls(), configuration.getSearchAuthKey());
    final Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    Log.sysLog(getSendername() + ": days entries: " + shows.size());

    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {

    int daysPast = 7;
    int daysFuture = 5;

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
         i
                 <= daysPast
                 + daysFuture;
         i++) {

      final LocalDateTime local
              = LocalDateTime.now()
              .plus(daysFuture, ChronoUnit.DAYS)
              .minus(i, ChronoUnit.DAYS);
      final String date = local.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
      final String url = String.format(getUrlDay(), date, date);
      urls.add(new CrawlerUrlDTO(url));
    }

    return urls;
  }
  private @NotNull String getUrlDay() {
    return ZdfConstants.URL_DAY;
  }

  private String getApiUrlBase() {
    return ZdfConstants.URL_API_BASE;
  }

  private @NotNull String getUrlBase() {
    return ZdfConstants.URL_BASE;
  }
}
