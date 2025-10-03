package de.mediathekview.mserver.crawler.sr;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.sr.tasks.SrFilmDetailTask;
import de.mediathekview.mserver.crawler.sr.tasks.SrTopicArchivePageTask;
import de.mediathekview.mserver.crawler.sr.tasks.SrTopicsOverviewPageTask;
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

public class SrCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(SrCrawler.class);

  public SrCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.SR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Queue<SrTopicUrlDTO> filmDtos = new ConcurrentLinkedQueue<>();

    try {
      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        final SrTopicsOverviewPageTask overviewTask = new SrTopicsOverviewPageTask(this);
        final Queue<SrTopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

        printMessage(
            ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

        final SrTopicArchivePageTask archiveTask = new SrTopicArchivePageTask(this, shows);
        filmDtos.addAll(forkJoinPool.submit(archiveTask).get());
      }

      printMessage(
              ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmDtos.size());
      getAndSetMaxCount(filmDtos.size());

      return new SrFilmDetailTask(this, filmDtos);
    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSender().getName(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSender().getName(), ex);
    }
    return null;
  }
}
