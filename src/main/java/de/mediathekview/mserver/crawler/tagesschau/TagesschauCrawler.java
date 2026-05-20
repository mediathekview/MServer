package de.mediathekview.mserver.crawler.tagesschau;

import de.mediathekview.mserver.crawler.tagesschau.tasks.TagesschauEntriesTask;
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
import java.util.HashSet;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
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
      Set<CrawlerUrlDTO> videos = new HashSet<>();
      Queue<CrawlerUrlDTO> inputQueue = createArchiveUrl();

      // short run uses 2 recursion -> only the actual month is included
      int recursionMax = Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled()) ? 10 : 2;
      int recursionCount = 0;

      while (!inputQueue.isEmpty() && recursionCount < recursionMax) {
        LOG.debug("processing {} sub pages", inputQueue.size());
        TagesschauEntriesTask round1 = new TagesschauEntriesTask(this, inputQueue);
        final Set<EntryUrlDto> results = this.forkJoinPool.submit(round1).get();

        Set<CrawlerUrlDTO> subPages = new HashSet<>();
        results.forEach(
            result -> {
              videos.addAll(result.getVideos());
              subPages.addAll(result.getSubPages());
            });
        inputQueue = new ConcurrentLinkedQueue<>(subPages);
        recursionCount++;
      }

      final Queue<CrawlerUrlDTO> videosFiltered = this.filterExistingFilms(videos, CrawlerUrlDTO::getUrl);
      getAndSetMaxCount(videosFiltered.size());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT,
          getSender().getName(),
              videosFiltered.size());

      return new TagesschauVideoTask(this, new ConcurrentLinkedQueue<>(videosFiltered));

    } catch (final InterruptedException ex) {
      LOG.fatal("Exception in Tagesschau crawler.", ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in Tagesschau crawler.", ex);
    }
    return new RecursiveTask<>() {
      @Override
      protected Set<Film> compute() {
        return Set.of();
      }
    };
  }

  private Queue<CrawlerUrlDTO> createArchiveUrl() {
    Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(TagesschauConstants.ARCHIVE_START_URL));
    return urls;
  }
}


