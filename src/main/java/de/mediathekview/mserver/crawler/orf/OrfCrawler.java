package de.mediathekview.mserver.crawler.orf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.orf.tasks.OrfDayTask;
import de.mediathekview.mserver.crawler.orf.tasks.OrfFilmDetailTask;
import de.mediathekview.mserver.crawler.orf.tasks.OrfLetterPageTask;
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

public class OrfCrawler extends AbstractCrawler {
  
  private static final Logger LOG = LogManager.getLogger(OrfCrawler.class);  

  public OrfCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }
  
  @Override
  public Sender getSender() {
    return Sender.ORF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      OrfLetterPageTask letterTask = new OrfLetterPageTask();
      ConcurrentLinkedQueue<OrfTopicUrlDTO> shows = forkJoinPool.submit(letterTask).get();

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      OrfDayTask dayTask = new OrfDayTask(this, getDayUrls());
      Set<OrfTopicUrlDTO> shows1 = forkJoinPool.submit(dayTask).get();
      
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows1.size());
      
      shows1.forEach(show -> {
        if (!shows.contains(show)) {
          shows.add(show);
        }
      });
      
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());
      
      return new OrfFilmDetailTask(this, shows);
    } catch (InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in ORF crawler.", ex);
    }
    return null;
  }
  
  private ConcurrentLinkedQueue<CrawlerUrlDTO> getDayUrls() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      urls.add(new CrawlerUrlDTO(OrfConstants.URL_DAY + 
          LocalDateTime.now().minus(i, ChronoUnit.DAYS).format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))));
    }

    return urls;
  }
}
