package de.mediathekview.mserver.crawler.sr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.sr.tasks.SrFilmDetailTask;
import de.mediathekview.mserver.crawler.sr.tasks.SrTopicArchivePageTask;
import de.mediathekview.mserver.crawler.sr.tasks.SrTopicsOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SrCrawler extends AbstractCrawler {
  
  private static final Logger LOG = LogManager.getLogger(SrCrawler.class);  

  public SrCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }
  
  @Override
  public Sender getSender() {
    return Sender.SR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      SrTopicsOverviewPageTask overviewTask = new SrTopicsOverviewPageTask();
      ConcurrentLinkedQueue<SrTopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      SrTopicArchivePageTask archiveTask = new SrTopicArchivePageTask(this, shows);
      ConcurrentLinkedQueue<SrTopicUrlDTO> filmDtos = new ConcurrentLinkedQueue<>();
      filmDtos.addAll(forkJoinPool.submit(archiveTask).get());

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmDtos.size());
      getAndSetMaxCount(filmDtos.size());

      return new SrFilmDetailTask(this, filmDtos);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in SR crawler.", ex);
    }
    return null;
  }  
}
