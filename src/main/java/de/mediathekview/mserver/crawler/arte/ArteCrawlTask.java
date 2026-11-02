package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.daten.Film;
import java.io.Serial;
import java.util.*;
import java.util.concurrent.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/** One catalogue scan, followed by at most four concurrent player resolutions. */
final class ArteCrawlTask extends RecursiveTask<Set<Film>> {
  @Serial private static final long serialVersionUID = 1L;
  private static final Logger LOG = LogManager.getLogger(ArteCrawlTask.class);
  private final AbstractCrawler crawler;
  private final String language;
  private volatile ExecutorService workers;
  private volatile ArteHttpClient transport;

  ArteCrawlTask(AbstractCrawler crawler, String language) {
    this.crawler = crawler;
    this.language = language;
  }

  @Override
  protected Set<Film> compute() {
    Set<Film> films = ConcurrentHashMap.newKeySet();
    try (var http = new ArteHttpClient(crawler.getCrawlerConfig().getSocketTimeoutInSeconds(), crawler.getRateLimiter())) {
      transport = http;
      var api = new ArteCatalogue(language, crawler.getSender(), crawler.getCrawlerConfig().getMaximumSubpages(),
          url -> {
            if (isCancelled()) throw new CancellationException("ARTE crawl cancelled");
            return http.get(url);
          }, this::reportError);
      var programmes = crawler.filterExistingFilms(api.discover(), ArteCatalogue.Programme::id);
      crawler.getAndSetMaxCount(programmes.size());
      crawler.updateProgress();
      var pending = new ConcurrentLinkedQueue<>(programmes);
      try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
        workers = executor;
        if (isCancelled()) return films;
        var futures = new ArrayList<Future<?>>();
        int concurrency = Math.min(4, Math.max(1, crawler.getRuntimeConfig().getMaximumCpuThreads()));
        for (int i = 0; i < concurrency; i++) {
          futures.add(executor.submit(() -> {
            ArteCatalogue.Programme programme;
            while (!isCancelled() && !Thread.currentThread().isInterrupted() && (programme = pending.poll()) != null) {
              try {
                films.addAll(api.films(programme));
              } catch (CancellationException e) {
                throw e;
              } catch (Exception e) {
                if (!isCancelled() && !Thread.currentThread().isInterrupted()) reportError(e);
              } finally {
                crawler.incrementAndGetActualCount();
                crawler.updateProgress();
              }
            }
          }));
        }
        try {
          for (var future : futures) future.get();
        } finally {
          executor.shutdownNow();
        }
      }
    } catch (CancellationException _) {
      // Cancellation is a lifecycle event, not a failed ARTE request.
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    } catch (Exception e) {
      if (!isCancelled()) reportError(e);
    } finally {
      workers = null;
      transport = null;
    }
    return films;
  }

  private void reportError(Exception exception) {
    crawler.incrementAndGetErrorCount();
    LOG.warn("ARTE {} request/conversion failed", language, exception);
  }

  @Override
  public boolean cancel(boolean mayInterruptIfRunning) {
    boolean cancelled = super.cancel(mayInterruptIfRunning);
    var executor = workers;
    if (executor != null) executor.shutdownNow();
    var http = transport;
    if (http != null) http.close();
    return cancelled;
  }
}
