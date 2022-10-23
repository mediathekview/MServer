package de.mediathekview.mserver.crawler.dw;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.dw.tasks.DWOverviewTask;
import de.mediathekview.mserver.crawler.dw.tasks.DwFilmDetailTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DwCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(DwCrawler.class);
  
  public DwCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.DW;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    Queue<CrawlerUrlDTO> shows =new ConcurrentLinkedQueue<>();
    try {
      shows.addAll(getShows());
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new DwFilmDetailTask(this,shows);
      
    } catch (final ExecutionException executionException) {
      LOG.fatal("Exception in DW crawler.", executionException);
    } catch (final InterruptedException interruptedException) {
      LOG.fatal("Exception in DW crawler.", interruptedException);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  private Collection<CrawlerUrlDTO> getShows() throws ExecutionException, InterruptedException {
    final CrawlerUrlDTO url = new CrawlerUrlDTO(DwConstants.URL_BASE + DwConstants.URL_OVERVIEW);

    final Queue<CrawlerUrlDTO> startUrl = new ConcurrentLinkedQueue<>();
    startUrl.add(url);

    final DWOverviewTask overviewTask = new DWOverviewTask(this, startUrl, 0);
    return forkJoinPool.submit(overviewTask).get();

  }
}
