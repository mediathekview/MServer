package mServer.crawler.sender.kika;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsoupConnection;
import mServer.crawler.sender.kika.KikaCrawlerUrlDto.FilmType;
import mServer.crawler.sender.kika.tasks.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RecursiveTask;

public class KikaCrawler extends MediathekCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);

  JsoupConnection jsoupConnection;

  public KikaCrawler(FilmeSuchen ssearch, int startPrio) {
       super(ssearch, Const.KIKA, 0, 1, startPrio);
    jsoupConnection = new JsoupConnection();
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {
    final ConcurrentLinkedQueue<KikaCrawlerUrlDto> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();

    try {
      sendungsfolgenUrls.addAll(getDaysEntries());
    } catch (final ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    if (CrawlerTool.loadLongMax()) {
      try {
        final Set<KikaCrawlerUrlDto> topicOverviewUrls = new HashSet<>();
        topicOverviewUrls.addAll(getAudioDescriptionAndSignLanguageEntries());
        topicOverviewUrls.addAll(getLetterEntries());

        topicOverviewUrls.forEach(
                show -> {
                  if (!sendungsfolgenUrls.contains(show)) {
                    sendungsfolgenUrls.add(show);
                  }
                });
      } catch (final ExecutionException | InterruptedException ex) {
        LOG.fatal("Exception in KIKA crawler.", ex);
      }
    }

    Log.sysLog("KIKA: Anzahl sendungsfolgen urls: " + sendungsfolgenUrls.size());

    final KikaSendungsfolgeVideoUrlTask sendungsfolgeVideoUrlsTask =
        new KikaSendungsfolgeVideoUrlTask(this, sendungsfolgenUrls, jsoupConnection);
    final Set<KikaCrawlerUrlDto> sendungsfolgeVideoUrls =
        forkJoinPool.invoke(sendungsfolgeVideoUrlsTask);

    Log.sysLog("KIKA Anzahl: " + sendungsfolgeVideoUrls.size());

    meldungAddMax(sendungsfolgeVideoUrls.size());

    return new KikaSendungsfolgeVideoDetailsTask(
        this, new ConcurrentLinkedQueue<>(sendungsfolgeVideoUrls));
  }

  private Set<KikaCrawlerUrlDto> getLetterEntries() throws InterruptedException, ExecutionException {
    final ConcurrentLinkedQueue<KikaCrawlerUrlDto> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new KikaCrawlerUrlDto(KikaConstants.URL_TOPICS_PAGE, FilmType.NORMAL));
    final KikaLetterPageUrlTask letterUrlTask =
        new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<KikaCrawlerUrlDto> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterTask =
        new KikaLetterPageTask(
            this, new ConcurrentLinkedQueue<>(letterUrls), KikaConstants.BASE_URL, jsoupConnection);
    final Set<KikaCrawlerUrlDto> topicUrls = forkJoinPool.submit(letterTask).get();

    final KikaTopicLandingPageTask landingTask =
        new KikaTopicLandingPageTask(
            this, new ConcurrentLinkedQueue<>(topicUrls), KikaConstants.BASE_URL, jsoupConnection);
    final Set<KikaCrawlerUrlDto> topicOverviewUrls = forkJoinPool.submit(landingTask).get();

    final KikaTopicOverviewPageTask topicOverviewTask =
        new KikaTopicOverviewPageTask(
            this,
            new ConcurrentLinkedQueue<>(topicOverviewUrls),
            KikaConstants.BASE_URL,
            jsoupConnection);
    Set<KikaCrawlerUrlDto> urls = forkJoinPool.submit(topicOverviewTask).get();
    LOG.info("KIKA: urls from topics: {}", urls.size());
    return urls;
  }

  private Set<KikaCrawlerUrlDto> getAudioDescriptionAndSignLanguageEntries()
      throws ExecutionException, InterruptedException {
    final ConcurrentLinkedQueue<KikaCrawlerUrlDto> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new KikaCrawlerUrlDto(KikaConstants.URL_DGS_PAGE, FilmType.SIGN_LANGUAGE));
    letterPageUrls.add(new KikaCrawlerUrlDto(KikaConstants.URL_AUDIO_DESCRIPTION_PAGE, FilmType.AUDIO_DESCRIPTION));
    final KikaLetterPageUrlTask letterUrlTask =
        new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<KikaCrawlerUrlDto> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterPageTask =
        new KikaLetterPageTask(
            this,
            new ConcurrentLinkedQueue<>(letterUrls),
            KikaConstants.BASE_URL,
            jsoupConnection);

    return forkJoinPool.submit(letterPageTask).get();
  }

  private Set<KikaCrawlerUrlDto> getDaysEntries() throws ExecutionException, InterruptedException {
    final Set<KikaCrawlerUrlDto> filmUrls = new HashSet<>();

    final KikaSendungVerpasstOverviewUrlTask daysOverviewUrlTask =
        new KikaSendungVerpasstOverviewUrlTask(this, LocalDateTime.now());

    final Set<CrawlerUrlDTO> daysUrls = forkJoinPool.submit(daysOverviewUrlTask).get();

    final KikaSendungVerpasstTask dayTask =
        new KikaSendungVerpasstTask(
            this, new ConcurrentLinkedQueue<>(daysUrls), KikaConstants.BASE_URL, jsoupConnection);
    filmUrls.addAll(forkJoinPool.invoke(dayTask));

    return filmUrls;
  }
}
