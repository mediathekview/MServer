package de.mediathekview.mserver.crawler.mdr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.tasks.MdrDayPageTask;
import de.mediathekview.mserver.crawler.mdr.tasks.MdrDayPageUrlTask;
import de.mediathekview.mserver.crawler.mdr.tasks.MdrFilmTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MdrCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(MdrCrawler.class);

  public MdrCrawler(ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.MDR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();
    try {
      shows.addAll(getDaysEntries());
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      getAndSetMaxCount(shows.size());

      return new MdrFilmTask(this, shows, MdrConstants.URL_BASE);
    } catch (ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in MDR crawler.", ex);
    }
    return null;
  }

  private Set<CrawlerUrlDTO> getDaysEntries() throws ExecutionException, InterruptedException {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrl = new ConcurrentLinkedQueue<>();
    dayUrl.add(new CrawlerUrlDTO(MdrConstants.URL_DAY_PAGE));

    MdrDayPageUrlTask dayUrlTask = new MdrDayPageUrlTask(this, dayUrl, crawlerConfig.getMaximumDaysForSendungVerpasstSection());

    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayPageUrls = new ConcurrentLinkedQueue<>();
    dayPageUrls.addAll(forkJoinPool.submit(dayUrlTask).get());

    MdrDayPageTask dayPageTask = new MdrDayPageTask(this, dayPageUrls);
    Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayPageTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }
}
