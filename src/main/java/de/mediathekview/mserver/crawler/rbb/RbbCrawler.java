package de.mediathekview.mserver.crawler.rbb;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
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
      ConcurrentLinkedQueue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();
      shows.addAll(getTopicsPageEntries());
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in RBB crawler.", ex);
    }

    return null;
  }

  private Set<TopicUrlDTO> getTopicsPageEntries() throws ExecutionException, InterruptedException {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> topicOverviews = new ConcurrentLinkedQueue<>();
    topicOverviews.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_A_K));
    topicOverviews.add(new CrawlerUrlDTO(RbbConstants.URL_TOPICS_L_Z));

    final ConcurrentLinkedQueue<TopicUrlDTO> topicPages = new ConcurrentLinkedQueue<>();

    RbbTopicsOverviewTask overviewTask = new RbbTopicsOverviewTask(this, topicOverviews);
    topicPages.addAll(forkJoinPool.submit(overviewTask).get());

    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), topicPages.size());

    RbbTopicOverviewTask topicTask = new RbbTopicOverviewTask(this, topicPages);
    return forkJoinPool.submit(topicTask).get();
  }
}
