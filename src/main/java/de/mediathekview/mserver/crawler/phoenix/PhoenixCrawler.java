package de.mediathekview.mserver.crawler.phoenix;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.phoenix.tasks.PhoenixFilmDetailTask;
import de.mediathekview.mserver.crawler.phoenix.tasks.PhoenixOverviewTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfFilmDetailTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class PhoenixCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(PhoenixCrawler.class);

  public PhoenixCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.PHOENIX;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    Queue<CrawlerUrlDTO> shows =new ConcurrentLinkedQueue<>();
    // TODO Dauer fehlt
    // TODO phoenix vor ort fehlt => müssten mehrere Videos pro Seite sein!

    try {
      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        shows.addAll(getShows());
      }
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new PhoenixFilmDetailTask(
          this, shows, null, PhoenixConstants.URL_BASE);
    } catch (final ExecutionException executionException) {
      LOG.fatal("Exception in Phönix crawler.", executionException);
    } catch (final InterruptedException interruptedException) {
      LOG.fatal("Exception in Phönix crawler.", interruptedException);
      Thread.currentThread().interrupt();
    }

    return null;
  }

  private Collection<CrawlerUrlDTO> getShows() throws ExecutionException, InterruptedException {
    // load sendungen page
    final CrawlerUrlDTO url =
        new CrawlerUrlDTO(PhoenixConstants.URL_BASE + PhoenixConstants.URL_OVERVIEW_JSON);

    final Queue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(url);

    final Set<CrawlerUrlDTO> overviewUrls = loadOverviewPages(queue);

    // load sendung overview pages
    final Queue<CrawlerUrlDTO> queue1 = new ConcurrentLinkedQueue<>(overviewUrls);
    return loadOverviewPages(queue1);
  }

  private Set<CrawlerUrlDTO> loadOverviewPages(final Queue<CrawlerUrlDTO> aQueue)
      throws ExecutionException, InterruptedException {
    final PhoenixOverviewTask overviewTask =
        new PhoenixOverviewTask(this, aQueue, null, PhoenixConstants.URL_BASE);
    return forkJoinPool.submit(overviewTask).get();
  }
}
