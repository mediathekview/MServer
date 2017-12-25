package de.mediathekview.mserver.crawler.kika;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.tasks.KikaPagedOverviewPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungOverviewPageTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungVerpasstTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgeVideoDetailsTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgeVideoUrlTask;
import de.mediathekview.mserver.crawler.kika.tasks.KikaSendungsfolgenOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class KikaCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaCrawler.class);
  // http://www.kika.de/sendungen/ipg/ipg102.html#date-22122017
  public static final String BASE_URL = "http://www.kika.de/sendungen/";
  private static final String SENDUNG_VERPASST_URL_TEMPLATE = BASE_URL + "ipg/ipg102.html#date-%s";
  public static final String SENDUNGEN_OVERVIEW_PAGE_URL = BASE_URL + "sendungenabisz100.html";
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("ddMMyyyy");

  public KikaCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getSendungVerpasstStartUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      urls.add(new CrawlerUrlDTO(String.format(SENDUNG_VERPASST_URL_TEMPLATE,
          LocalDateTime.now().minus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER))));
    }

    return urls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();

    // Gathers all Sendungen
    final Set<CrawlerUrlDTO> sendungenOverviewPageUrls = new HashSet<>();
    sendungenOverviewPageUrls.add(new CrawlerUrlDTO(SENDUNGEN_OVERVIEW_PAGE_URL));
    final KikaPagedOverviewPageTask sendungenOverviewPageTask =
        new KikaPagedOverviewPageTask(this, new ConcurrentLinkedQueue<>(sendungenOverviewPageUrls));

    // Gathers the URLs for the Sendungsfolgen overview page from the Sendungen pages.
    final Set<CrawlerUrlDTO> sendungOverviewPageUrls =
        forkJoinPool.invoke(sendungenOverviewPageTask);
    final KikaSendungOverviewPageTask sendungOverviewPageTask =
        new KikaSendungOverviewPageTask(this, new ConcurrentLinkedQueue<>(sendungOverviewPageUrls));

    // Gathers the Sendungsfolgen URLs from the Sendungsfolgen overview page.
    final Set<CrawlerUrlDTO> sendungsfolgenOverviewPageUrls =
        forkJoinPool.invoke(sendungOverviewPageTask);
    final KikaSendungsfolgenOverviewPageTask sendungsfolgenOverviewPageTask =
        new KikaSendungsfolgenOverviewPageTask(this,
            new ConcurrentLinkedQueue<>(sendungsfolgenOverviewPageUrls));

    final ForkJoinTask<Set<CrawlerUrlDTO>> featureSendungsFolgenUrls =
        forkJoinPool.submit(sendungsfolgenOverviewPageTask);

    final KikaSendungVerpasstTask sendungVerpasstTask =
        new KikaSendungVerpasstTask(this, getSendungVerpasstStartUrls());
    sendungsfolgenUrls.addAll(forkJoinPool.invoke(sendungVerpasstTask));
    try {
      sendungsfolgenUrls.addAll(featureSendungsFolgenUrls.get());
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong on gathering the Sendungsfolgen.");
      printErrorMessage();
    }

    final KikaSendungsfolgeVideoUrlTask sendungsfolgeVideoUrlsTask =
        new KikaSendungsfolgeVideoUrlTask(this, sendungsfolgenUrls);
    final Set<CrawlerUrlDTO> sendungsfolgeVideoUrls =
        forkJoinPool.invoke(sendungsfolgeVideoUrlsTask);
    return new KikaSendungsfolgeVideoDetailsTask(this,
        new ConcurrentLinkedQueue<>(sendungsfolgeVideoUrls));
  }

}
