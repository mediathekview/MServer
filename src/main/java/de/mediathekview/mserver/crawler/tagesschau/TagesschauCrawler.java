package de.mediathekview.mserver.crawler.tagesschau;

import de.mediathekview.mserver.crawler.tagesschau.tasks.TagesschauEnriesTask;
import de.mediathekview.mserver.crawler.tagesschau.tasks.TagesschauOverviewTask;
import de.mediathekview.mserver.crawler.tagesschau.tasks.TagesschauVideoTask;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class TagesschauCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(TagesschauCrawler.class);

  public TagesschauCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.TAGESSCHAU24;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      Queue<CrawlerUrlDTO> archiveUrl = createArchiveUrl();

      TagesschauOverviewTask overviewTask = new TagesschauOverviewTask(this, archiveUrl);
      final Set<CrawlerUrlDTO> overviewResults = this.forkJoinPool.submit(overviewTask).get();

      // TODO nur für den aktuellen Monat passt die Logik
      // für alle anderen Einträge muss rekursive OverviewTask genutzt werden, bis die Monatsseite erreicht ist

      LOG.debug("Overview task completed. Found {} overview URLs.", overviewResults.size());

      TagesschauEnriesTask entriesTask = new TagesschauEnriesTask(this, new ConcurrentLinkedQueue<>(overviewResults));
      final Set<CrawlerUrlDTO> entriesResults = this.forkJoinPool.submit(entriesTask).get();

      LOG.debug("Entries task completed. Found {} entry URLs.", entriesResults.size());

      getAndSetMaxCount(entriesResults.size());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT,
          getSender().getName(),
              entriesResults.size());

      return new TagesschauVideoTask(this, new ConcurrentLinkedQueue<>(entriesResults));

    } catch (final Exception ex) {
      LOG.fatal("Exception in Tagesschau crawler.", ex);
      printErrorMessage();
    }
    return null;
  }

  private Queue<CrawlerUrlDTO> createArchiveUrl() {
    Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(TagesschauConstants.ARCHIVE_START_URL));
    return urls;
  }
}


