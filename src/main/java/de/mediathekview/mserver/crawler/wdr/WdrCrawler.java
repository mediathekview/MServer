package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrDayPageTask;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrFilmDetailTask;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrLetterPageTask;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrTopicOverviewTask;
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

public class WdrCrawler extends AbstractCrawler {
  
  private static final Logger LOG = LogManager.getLogger(WdrCrawler.class);  

  public WdrCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.WDR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      ConcurrentLinkedQueue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>();
      
      WdrDayPageTask dayTask = new WdrDayPageTask(this, getDayUrls());
      shows.addAll(forkJoinPool.submit(dayTask).get());

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      
      WdrLetterPageTask letterTask = new WdrLetterPageTask();
      ConcurrentLinkedQueue<WdrTopicUrlDTO> letterPageEntries = forkJoinPool.submit(letterTask).get();

      WdrTopicOverviewTask overviewTask = new WdrTopicOverviewTask(this, letterPageEntries);
      shows.addAll(forkJoinPool.submit(overviewTask).get());
      
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      
      getAndSetMaxCount(shows.size());

      return new WdrFilmDetailTask(this, shows);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in WDR crawler.", ex);
    }
    return null;
  }
  
  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      urls.add(new CrawlerUrlDTO(String.format(WdrConstants.URL_DAY, 
          LocalDateTime.now().minus(i, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("ddMMyyyy")))));
    }

    return urls;
  }  
}
