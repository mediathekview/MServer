package de.mediathekview.mserver.crawler.dreisat;

import java.util.Collection;
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
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatFilmDetailsTask;
import de.mediathekview.mserver.crawler.dreisat.tasks.DreisatOverviewpageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class DreiSatCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(DreiSatCrawler.class);
  private static final String SENDUNG_VERPASST_BASE_URL =
      "http://www.3sat.de/mediathek/?mode=verpasst";
  private static final String SENDUNGEN_AZ_URL = "http://www.3sat.de/mediathek/?mode=sendungenaz";

  public DreiSatCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners);
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
    sendungVerpasstUrls.add(new CrawlerUrlDTO(SENDUNG_VERPASST_BASE_URL));
    return sendungVerpasstUrls;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    final DreisatOverviewpageTask sendungenTask = new DreisatOverviewpageTask(this,
        getSendungenAZUrls(), false, config.getMaximumDaysForSendungVerpasstSection());
    final Set<CrawlerUrlDTO> sendungUrls = forkJoinPool.invoke(sendungenTask);

    final DreisatOverviewpageTask sendungsfolgenTask = new DreisatOverviewpageTask(this,
        new ConcurrentLinkedQueue<>(sendungUrls), true, config.getMaximumSubpages());
    final ForkJoinTask<Set<CrawlerUrlDTO>> featureFendungsfolgenFilmUrls =
        forkJoinPool.submit(sendungsfolgenTask);


    final DreisatOverviewpageTask sendungVerpasstTask = new DreisatOverviewpageTask(this,
        getSendungVerpasstUrls(), true, config.getMaximumSubpages());

    final ConcurrentLinkedQueue<CrawlerUrlDTO> filmUrls = new ConcurrentLinkedQueue<>();
    try {
      filmUrls.addAll(forkJoinPool.invoke(sendungVerpasstTask));
      filmUrls.addAll(featureFendungsfolgenFilmUrls.get());

    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong on gathering the films.");
      printErrorMessage();
    }

    return new DreisatFilmDetailsTask(this, filmUrls);
  }

}
