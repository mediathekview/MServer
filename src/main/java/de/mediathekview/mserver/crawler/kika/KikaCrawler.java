package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.tasks.KikaLetterPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaLetterPageUrlTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungVerpasstOverviewUrlTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungVerpasstTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgeVideoDetailsTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgeVideoUrlTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaTopicLandingPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaTopicOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class KikaCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);

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
    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();

    try {
      sendungsfolgenUrls.addAll(getDaysEntries());
    } catch (ExecutionException | InterruptedException ex) {
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
    } catch (ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    printMessage(ServerMessages.DEBUG_KIKA_SENDUNGSFOLGEN_URL_CONVERTING, getSender().getName());
    final KikaSendungsfolgeVideoUrlTask sendungsfolgeVideoUrlsTask =
        new KikaSendungsfolgeVideoUrlTask(this, sendungsfolgenUrls);
    final Set<CrawlerUrlDTO> sendungsfolgeVideoUrls =
        forkJoinPool.invoke(sendungsfolgeVideoUrlsTask);
    printMessage(ServerMessages.DEBUG_KIKA_CONVERTING_FINISHED, getSender().getName());
    getAndSetMaxCount(sendungsfolgeVideoUrls.size());
    return new KikaSendungsfolgeVideoDetailsTask(
        this, new ConcurrentLinkedQueue<>(sendungsfolgeVideoUrls));
  }

  private Set<CrawlerUrlDTO> getLetterEntries() throws InterruptedException, ExecutionException {
    ConcurrentLinkedQueue<CrawlerUrlDTO> letterPageUrls = new ConcurrentLinkedQueue<>();
    letterPageUrls.add(new CrawlerUrlDTO(KikaConstants.URL_TOPICS_PAGE));
    final KikaLetterPageUrlTask letterUrlTask =
        new KikaLetterPageUrlTask(this, letterPageUrls, KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> letterUrls = forkJoinPool.submit(letterUrlTask).get();

    final KikaLetterPageTask letterTask =
        new KikaLetterPageTask(
            this, new ConcurrentLinkedQueue<>(letterUrls), KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> topicUrls = forkJoinPool.submit(letterTask).get();

    final KikaTopicLandingPageTask landingTask =
        new KikaTopicLandingPageTask(
            this, new ConcurrentLinkedQueue<>(topicUrls), KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> topicOverviewUrls = forkJoinPool.submit(landingTask).get();

    final KikaTopicOverviewPageTask topicOverviewTask =
        new KikaTopicOverviewPageTask(
            this, new ConcurrentLinkedQueue<>(topicOverviewUrls), KikaConstants.BASE_URL);
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
            this, new ConcurrentLinkedQueue<>(daysUrls), KikaConstants.BASE_URL);
    filmUrls.addAll(forkJoinPool.invoke(dayTask));

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmUrls.size());

    return filmUrls;
  }
}
