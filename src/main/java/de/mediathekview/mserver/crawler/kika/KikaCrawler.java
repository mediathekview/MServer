package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class KikaCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);

  JsoupConnection jsoupConnection;

  public KikaCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);

    jsoupConnection = new JsoupConnection();
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Queue<CrawlerUrlDTO> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();

    try {
      sendungsfolgenUrls.addAll(getDaysEntries());
    } catch (final ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }
    try {
      getLetterEntries()
          .forEach(
              show -> {
                if (!sendungsfolgenUrls.contains(show)) {
                  sendungsfolgenUrls.add(show);
                }
              });
    } catch (final ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    printMessage(ServerMessages.DEBUG_KIKA_SENDUNGSFOLGEN_URL_CONVERTING, getSender().getName());
    final KikaSendungsfolgeVideoUrlTask sendungsfolgeVideoUrlsTask =
        new KikaSendungsfolgeVideoUrlTask(this, sendungsfolgenUrls, jsoupConnection);
    final Set<CrawlerUrlDTO> sendungsfolgeVideoUrls =
        forkJoinPool.invoke(sendungsfolgeVideoUrlsTask);
    printMessage(ServerMessages.DEBUG_KIKA_CONVERTING_FINISHED, getSender().getName());
    getAndSetMaxCount(sendungsfolgeVideoUrls.size());
    return new KikaSendungsfolgeVideoDetailsTask(
        this, new ConcurrentLinkedQueue<>(sendungsfolgeVideoUrls));
  }

  private Set<CrawlerUrlDTO> getLetterEntries() throws InterruptedException, ExecutionException {
    final Queue<CrawlerUrlDTO> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new CrawlerUrlDTO(KikaConstants.URL_TOPICS_PAGE));
    final KikaLetterPageUrlTask letterUrlTask =
        new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<CrawlerUrlDTO> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterTask =
        new KikaLetterPageTask(
            this, new ConcurrentLinkedQueue<>(letterUrls), KikaConstants.BASE_URL, jsoupConnection);
    final Set<CrawlerUrlDTO> topicUrls = forkJoinPool.submit(letterTask).get();

    final KikaTopicLandingPageTask landingTask =
        new KikaTopicLandingPageTask(
            this, new ConcurrentLinkedQueue<>(topicUrls), KikaConstants.BASE_URL, jsoupConnection);
    final Set<CrawlerUrlDTO> topicOverviewUrls = forkJoinPool.submit(landingTask).get();

    final KikaTopicOverviewPageTask topicOverviewTask =
        new KikaTopicOverviewPageTask(
            this,
            new ConcurrentLinkedQueue<>(topicOverviewUrls),
            KikaConstants.BASE_URL,
            jsoupConnection);
    return forkJoinPool.submit(topicOverviewTask).get();
  }

  private Set<CrawlerUrlDTO> getDaysEntries() throws ExecutionException, InterruptedException {
    final Set<CrawlerUrlDTO> filmUrls = new HashSet<>();

    final KikaSendungVerpasstOverviewUrlTask daysOverviewUrlTask =
        new KikaSendungVerpasstOverviewUrlTask(this, LocalDateTime.now());

    final Set<CrawlerUrlDTO> daysUrls = forkJoinPool.submit(daysOverviewUrlTask).get();
    printMessage(
        ServerMessages.DEBUG_KIKA_SENDUNG_VERPASST_PAGES, daysUrls.size(), getSender().getName());

    final KikaSendungVerpasstTask dayTask =
        new KikaSendungVerpasstTask(
            this, new ConcurrentLinkedQueue<>(daysUrls), KikaConstants.BASE_URL, jsoupConnection);
    filmUrls.addAll(forkJoinPool.invoke(dayTask));

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmUrls.size());

    return filmUrls;
  }
}
