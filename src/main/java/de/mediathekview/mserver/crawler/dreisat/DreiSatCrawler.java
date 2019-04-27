package de.mediathekview.mserver.crawler.dreisat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatDayPageTask;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatFilmDetailsTask;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatOverviewpageTask;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatTopicPageTask;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatTopicsOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DreiSatCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(DreiSatCrawler.class);

  private static final String SENDUNG_VERPASST_BASE_URL =
      "https://www.3sat.de/mediathek/index.php?datum=%s";
  private static final String SENDUNGEN_AZ_URL = "https://www.3sat.de/mediathek/?mode=sendungenaz";

  private static final DateTimeFormatter SENDUNG_VERPASST_DATEFORMATTER =
      DateTimeFormatter.ofPattern("yyyyMMdd");

  public DreiSatCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.DREISAT;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getSendungenAZUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungUrls = new ConcurrentLinkedQueue<>();
    sendungUrls.add(new CrawlerUrlDTO(SENDUNGEN_AZ_URL));
    return sendungUrls;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getSendungVerpasstUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> sendungVerpasstUrls = new ConcurrentLinkedQueue<>();

    for (int i = 0;
        i
            < crawlerConfig.getMaximumDaysForSendungVerpasstSection()
            + crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture();
        i++) {
      sendungVerpasstUrls.add(
          new CrawlerUrlDTO(
              String.format(SENDUNG_VERPASST_BASE_URL,
                  LocalDateTime.now()
                      .plus(
                          crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(),
                          ChronoUnit.DAYS)
                      .minus(i, ChronoUnit.DAYS)
                      .format(SENDUNG_VERPASST_DATEFORMATTER))));
    }

    return sendungVerpasstUrls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    final DreisatTopicsOverviewPageTask sendungenTask = new DreisatTopicsOverviewPageTask(this,
        getSendungenAZUrls(), false);
    final Set<CrawlerUrlDTO> sendungUrls = forkJoinPool.invoke(sendungenTask);

    final DreisatTopicPageTask sendungsfolgenTask = new DreisatTopicPageTask(this,
        new ConcurrentLinkedQueue<>(sendungUrls), true, crawlerConfig.getMaximumSubpages());
    final ForkJoinTask<Set<CrawlerUrlDTO>> featureSendungsfolgenFilmUrls =
        forkJoinPool.submit(sendungsfolgenTask);

    final DreisatDayPageTask sendungVerpasstTask = new DreisatDayPageTask(this,
        getSendungVerpasstUrls(), true);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> filmUrls = new ConcurrentLinkedQueue<>();
    try {
      filmUrls.addAll(forkJoinPool.invoke(sendungVerpasstTask));
      filmUrls.addAll(featureSendungsfolgenFilmUrls.get());

    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong on gathering the films.");
      printErrorMessage();
    }

    return new DreisatFilmDetailsTask(this, filmUrls, "https://www.3sat.de", "https://tmd.3sat.de");
  }

}
