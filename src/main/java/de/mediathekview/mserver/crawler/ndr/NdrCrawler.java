package de.mediathekview.mserver.crawler.ndr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.ndr.tasks.NdrSendungVerpasstTask;
import de.mediathekview.mserver.crawler.ndr.tasks.NdrSendungenOverviewPageTask;
import de.mediathekview.mserver.crawler.ndr.tasks.NdrSendungsfolgedetailsTask;
import de.mediathekview.mserver.crawler.ndr.tasks.NdrSendungsfolgenOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class NdrCrawler extends AbstractCrawler {
  public static final String NDR_BASE_URL = "http://www.ndr.de/mediathek/";
  private static final Logger LOG = LogManager.getLogger(NdrCrawler.class);
  private static final String SENDUNG_VERPASST_URL_TEMPLATE =
      NDR_BASE_URL + "sendung_verpasst/epg1490_date-%s_display-all.html";
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  JsoupConnection jsoupConnection;

  public NdrCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);

    this.jsoupConnection = new JsoupConnection();
  }

  @Override
  public Sender getSender() {
    return Sender.NDR;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getSendungVerpasstStartUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
        i
            < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
                + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture();
        i++) {
      urls.add(
          new CrawlerUrlDTO(
              String.format(
                  SENDUNG_VERPASST_URL_TEMPLATE,
                  LocalDateTime.now()
                      .plus(
                          crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(),
                          ChronoUnit.DAYS)
                      .minus(i, ChronoUnit.DAYS)
                      .format(URL_DATE_TIME_FORMATTER))));
    }

    return urls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final NdrSendungenOverviewPageTask sendungenOverviewPageTask =
        new NdrSendungenOverviewPageTask(this);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();
    try {
      final NdrSendungsfolgenOverviewPageTask ndrSendungsfolgenOverviewPageTask =
          new NdrSendungsfolgenOverviewPageTask(
              this,
              new ConcurrentLinkedQueue<>(forkJoinPool.submit(sendungenOverviewPageTask).get()), jsoupConnection);

      final NdrSendungVerpasstTask sendungVerpasstTask =
          new NdrSendungVerpasstTask(this, getSendungVerpasstStartUrls(), jsoupConnection);

      sendungsfolgenUrls.addAll(forkJoinPool.invoke(ndrSendungsfolgenOverviewPageTask));
      sendungsfolgenUrls.addAll(forkJoinPool.invoke(sendungVerpasstTask));

    } catch (final InterruptedException | ExecutionException exception) {
      LOG.fatal("Something went terrible wrong on crawlin the NDR.", exception);
    }

    return new NdrSendungsfolgedetailsTask(this, sendungsfolgenUrls, jsoupConnection);
  }
}
