package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaCrawlerUrlDto.FilmType;
import de.mediathekview.mserver.crawler.kika.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Supplier;

public class KikaCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);

  static final Supplier<List<KikaCrawlerUrlDto>> ADDITIONAL_URLS = () -> {
    List<KikaCrawlerUrlDto> urls1 = new ArrayList<>();
    urls1.add(new KikaCrawlerUrlDto("https://www.kika.de/bernd-das-brot/bernd-klassiker-videos-100.html", FilmType.NORMAL));
    return urls1;
  };

  public KikaCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Queue<KikaCrawlerUrlDto> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();

    try {
      sendungsfolgenUrls.addAll(getDaysEntries());
    } catch (final ExecutionException executionException) {
      LOG.fatal("Exception in KIKA crawler.", executionException);
    } catch (final InterruptedException interruptedException) {
      LOG.fatal("KIKA crawler got interrupted", interruptedException);
      Thread.currentThread().interrupt();
    }
    try {
      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {

        final Set<KikaCrawlerUrlDto> topicOverviewUrls = new HashSet<>();
        topicOverviewUrls.addAll(getAudioDescriptionAndSignLanguageEntries());
        topicOverviewUrls.addAll(getLetterEntries());

        topicOverviewUrls.forEach(
            show -> {
              if (!sendungsfolgenUrls.contains(show)) {
                sendungsfolgenUrls.add(show);
              }
            });
      }
    } catch (final ExecutionException executionException) {
      LOG.fatal("Exception in KIKA crawler.", executionException);
    } catch (final InterruptedException interruptedException) {
      LOG.fatal("KIKA crawler got interrupted", interruptedException);
      Thread.currentThread().interrupt();
    }

    printMessage(ServerMessages.DEBUG_KIKA_SENDUNGSFOLGEN_URL_CONVERTING, getSender().getName());
    final KikaSendungsfolgeVideoUrlTask sendungsfolgeVideoUrlsTask =
        new KikaSendungsfolgeVideoUrlTask(this, sendungsfolgenUrls);
    final Set<KikaCrawlerUrlDto> sendungsfolgeVideoUrls =
        forkJoinPool.invoke(sendungsfolgeVideoUrlsTask);
    printMessage(ServerMessages.DEBUG_KIKA_CONVERTING_FINISHED, getSender().getName());
    getAndSetMaxCount(sendungsfolgeVideoUrls.size());
    return new KikaSendungsfolgeVideoDetailsTask(
        this, new ConcurrentLinkedQueue<>(sendungsfolgeVideoUrls));
  }

  private Set<KikaCrawlerUrlDto> getLetterEntries()
      throws InterruptedException, ExecutionException {
    final Queue<KikaCrawlerUrlDto> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new KikaCrawlerUrlDto(KikaConstants.URL_TOPICS_PAGE, FilmType.NORMAL));
    final KikaLetterPageUrlTask letterUrlTask =
        new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterTask =
        new KikaLetterPageTask(
            this, new ConcurrentLinkedQueue<>(letterUrls), KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> topicUrls = forkJoinPool.submit(letterTask).get();

    final KikaTopicLandingPageTask landingTask =
        new KikaTopicLandingPageTask(
            this, new ConcurrentLinkedQueue<>(topicUrls), KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> topicOverviewUrls = forkJoinPool.submit(landingTask).get();
    topicOverviewUrls.addAll(ADDITIONAL_URLS.get());

    final KikaTopicOverviewPageTask topicOverviewTask =
        new KikaTopicOverviewPageTask(
            this, new ConcurrentLinkedQueue<>(topicOverviewUrls), KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> urls = forkJoinPool.submit(topicOverviewTask).get();
    LOG.info("KIKA: urls from topics: {}", urls.size());
    return urls;
  }

  private Set<KikaCrawlerUrlDto> getAudioDescriptionAndSignLanguageEntries()
      throws ExecutionException, InterruptedException {
    final Queue<KikaCrawlerUrlDto> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new KikaCrawlerUrlDto(KikaConstants.URL_DGS_PAGE, FilmType.SIGN_LANGUAGE));
    letterPageUrls.add(
        new KikaCrawlerUrlDto(
            KikaConstants.URL_AUDIO_DESCRIPTION_PAGE, FilmType.AUDIO_DESCRIPTION));
    final KikaLetterPageUrlTask letterUrlTask =
        new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL);
    final Set<KikaCrawlerUrlDto> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterPageTask =
        new KikaLetterPageTask(
            this, new ConcurrentLinkedQueue<>(letterUrls), KikaConstants.BASE_URL);

    return forkJoinPool.submit(letterPageTask).get();
  }

  private Set<KikaCrawlerUrlDto> getDaysEntries() throws ExecutionException, InterruptedException {
    final Set<KikaCrawlerUrlDto> filmUrls = new HashSet<>();

    final KikaSendungVerpasstOverviewUrlTask daysOverviewUrlTask =
        new KikaSendungVerpasstOverviewUrlTask(this, LocalDateTime.now());

    final Set<CrawlerUrlDTO> daysUrls = forkJoinPool.submit(daysOverviewUrlTask).get();
    printMessage(
        ServerMessages.DEBUG_KIKA_SENDUNG_VERPASST_PAGES, daysUrls.size(), getSender().getName());

    final KikaSendungVerpasstTask dayTask =
        new KikaSendungVerpasstTask(
            this, new ConcurrentLinkedQueue<>(daysUrls), KikaConstants.BASE_URL);
    filmUrls.addAll(forkJoinPool.invoke(dayTask));

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmUrls.size());

    return filmUrls;
  }
}
