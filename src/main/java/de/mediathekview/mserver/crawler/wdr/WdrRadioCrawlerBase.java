package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrFilmDetailTask;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrTopicOverviewTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class WdrRadioCrawlerBase extends AbstractCrawler {
  
  protected static final Logger LOG = LogManager.getLogger(WdrRadioCrawlerBase.class);

  public WdrRadioCrawlerBase(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }
  
  protected abstract Set<WdrTopicUrlDTO> getTopicOverviewPages() throws InterruptedException, ExecutionException;

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      ConcurrentLinkedQueue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();
      shows.addAll(getEntries());
      
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());
      
      return new WdrFilmDetailTask(this, shows);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in WDR radio crawler.", ex);
    }
    return null;
  }

  protected Set<TopicUrlDTO> getEntries() throws InterruptedException, ExecutionException {
    ConcurrentLinkedQueue<WdrTopicUrlDTO> topicOverviews = new ConcurrentLinkedQueue<>();
    topicOverviews.addAll(getTopicOverviewPages());
    
    WdrTopicOverviewTask overviewTask = new WdrTopicOverviewTask(this, topicOverviews, 0);
    Set<TopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();
    
    printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
    return shows;
  }
}
