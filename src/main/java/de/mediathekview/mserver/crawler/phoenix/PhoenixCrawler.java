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
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhoenixCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(PhoenixCrawler.class);

  public PhoenixCrawler(ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.PHOENIX;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> shows = new ConcurrentLinkedQueue<>();

    try {
      shows.addAll(getShows());

      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(),
          shows.size());
      getAndSetMaxCount(shows.size());

      return new PhoenixFilmDetailTask(this, shows, Optional.empty(), PhoenixConstants.URL_BASE, PhoenixConstants.URL_VIDEO_DETAILS_HOST);
    } catch (ExecutionException | InterruptedException ex) {
      LOG.fatal("Exception in Phönix crawler.", ex);
    }

    return null;
  }

  private Collection<CrawlerUrlDTO> getShows() throws ExecutionException, InterruptedException {
    // load sendungen page
    CrawlerUrlDTO url = new CrawlerUrlDTO(PhoenixConstants.URL_BASE + PhoenixConstants.URL_OVERVIEW_JSON);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(url);

    final Set<CrawlerUrlDTO> overviewUrls = loadOverviewPages(queue);

    // load sendung overview pages
    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue1 = new ConcurrentLinkedQueue<>();
    queue1.addAll(overviewUrls);
    final Set<CrawlerUrlDTO> filmUrls = loadOverviewPages(queue1);

    return filmUrls;
  }

  private Set<CrawlerUrlDTO> loadOverviewPages(final ConcurrentLinkedQueue<CrawlerUrlDTO> aQueue)
      throws ExecutionException, InterruptedException {
    PhoenixOverviewTask overviewTask = new PhoenixOverviewTask(this, aQueue, Optional.empty(), PhoenixConstants.URL_BASE);
    final Set<CrawlerUrlDTO> urls = forkJoinPool.submit(overviewTask).get();

    return urls;
  }
}
