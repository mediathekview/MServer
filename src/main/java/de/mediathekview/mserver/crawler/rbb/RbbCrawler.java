package de.mediathekview.mserver.crawler.rbb;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.rbb.tasks.RbbDayTask;
import de.mediathekview.mserver.crawler.rbb.tasks.RbbFilmTask;
import de.mediathekview.mserver.crawler.rbb.tasks.RbbTopicOverviewTask;
import de.mediathekview.mserver.crawler.rbb.tasks.RbbTopicsOverviewTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RbbCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(RbbCrawler.class);

  public RbbCrawler(ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.RBB;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();
      shows.addAll(getTopicsPageEntries());

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      getDaysEntries().forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new RbbFilmTask(this, shows);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in RBB crawler.", ex);
    }

    return null;
  }

  private Set<CrawlerUrlDTO> getDaysEntries() throws ExecutionException, InterruptedException {
    RbbDayTask dayTask = new RbbDayTask(this, getDayUrls());
    Set<CrawlerUrlDTO> shows = forkJoinPool.submit(dayTask).get();

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    return shows;
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0;
        i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection() && i <= RbbConstants.MAX_SUPPORTED_DAYS_PAST;
        i++) {
      urls.add(new CrawlerUrlDTO(String.format(RbbConstants.URL_DAY_PAGE, i)));
    }

    return urls;
  }

  private Set<CrawlerUrlDTO> getTopicsPageEntries() throws ExecutionException, InterruptedException {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> topicOverviews = new ConcurrentLinkedQueue<>();
    topicOverviews.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_A_K));
    topicOverviews.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_L_Z));

    final ConcurrentLinkedQueue<CrawlerUrlDTO> topicPages = new ConcurrentLinkedQueue<>();

    RbbTopicsOverviewTask overviewTask = new RbbTopicsOverviewTask(this, topicOverviews);
    topicPages.addAll(forkJoinPool.submit(overviewTask).get());

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), topicPages.size());

    RbbTopicOverviewTask topicTask = new RbbTopicOverviewTask(this, topicPages);
    return forkJoinPool.submit(topicTask).get();
  }
}
