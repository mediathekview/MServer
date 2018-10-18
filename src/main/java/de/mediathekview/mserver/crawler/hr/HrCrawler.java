package de.mediathekview.mserver.crawler.hr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.tasks.HrSendungenOverviewPageTask;
import de.mediathekview.mserver.crawler.hr.tasks.HrSendungsfolgedetailsTask;
import de.mediathekview.mserver.crawler.hr.tasks.HrSendungsfolgenOverviewPageTask;
import de.mediathekview.mserver.crawler.hr.tasks.HrSendungsfolgenVerpasstOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HrCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(HrCrawler.class);
  private static final String SENDUNG_VERPASST_URL_TEMPLATE =
      HrConstants.BASE_URL + "tv-programm/guide_hrfernsehen-100~_date-%s.html";
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

  public HrCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.HR;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getSendungVerpasstStartUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
        + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(); i++) {
      urls.add(new CrawlerUrlDTO(String.format(SENDUNG_VERPASST_URL_TEMPLATE,
          LocalDateTime.now()
              .plus(crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(), ChronoUnit.DAYS)
              .minus(i, ChronoUnit.DAYS).format(URL_DATE_TIME_FORMATTER))));
    }

    return urls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final HrSendungenOverviewPageTask sendungenOverviewPageTask = new HrSendungenOverviewPageTask();

    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungsfolgenUrls = new ConcurrentLinkedQueue<>();
    try {
      final HrSendungsfolgenOverviewPageTask sendungsfolgenOverviewPageTask =
          new HrSendungsfolgenOverviewPageTask(this,
              new ConcurrentLinkedQueue<>(forkJoinPool.submit(sendungenOverviewPageTask).get()));

      final HrSendungsfolgenVerpasstOverviewPageTask sendungVerpasstTask =
          new HrSendungsfolgenVerpasstOverviewPageTask(this, getSendungVerpasstStartUrls());

      sendungsfolgenUrls.addAll(forkJoinPool.invoke(sendungsfolgenOverviewPageTask));
      sendungsfolgenUrls.addAll(forkJoinPool.invoke(sendungVerpasstTask));
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something went terrible wrong on crawlin the HR.", exception);
    }

    return new HrSendungsfolgedetailsTask(this, sendungsfolgenUrls);
  }

}
