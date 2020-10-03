package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrFilmDetailTask;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrTopicOverviewTask;
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

public abstract class WdrRadioCrawlerBase extends AbstractCrawler {

  protected static final Logger LOG = LogManager.getLogger(WdrRadioCrawlerBase.class);

  JsoupConnection jsoupConnection;

  public WdrRadioCrawlerBase(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);

    jsoupConnection = new JsoupConnection();
  }

  protected abstract Set<WdrTopicUrlDto> getTopicOverviewPages()
      throws InterruptedException, ExecutionException;

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      final Queue<TopicUrlDTO> shows = new ConcurrentLinkedQueue<>(getEntries());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
      getAndSetMaxCount(shows.size());

      return new WdrFilmDetailTask(this, shows, jsoupConnection);
    } catch (final ExecutionException executionException) {
      LOG.fatal("Exception in WDR radio crawler.", executionException);
    } catch (final InterruptedException interruptedException) {
      LOG.fatal("Exception in WDR radio crawler.", interruptedException);
      Thread.currentThread().interrupt();
    }
    return null;
  }

  protected Set<TopicUrlDTO> getEntries() throws InterruptedException, ExecutionException {
    final Queue<WdrTopicUrlDto> topicOverviews =
        new ConcurrentLinkedQueue<>(getTopicOverviewPages());

    final WdrTopicOverviewTask overviewTask =
        new WdrTopicOverviewTask(this, topicOverviews, jsoupConnection, 0);
    final Set<TopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

    printMessage(
        ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());
    return shows;
  }
}
