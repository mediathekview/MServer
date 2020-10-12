package de.mediathekview.mserver.crawler.sr;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
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

  JsoupConnection jsoupConnection;

  public SrCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);

    jsoupConnection = new JsoupConnection();
  }

  @Override
  public Sender getSender() {
    return Sender.SR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      final SrTopicsOverviewPageTask overviewTask =
          new SrTopicsOverviewPageTask(this, jsoupConnection);
      final Queue<SrTopicUrlDTO> shows = forkJoinPool.submit(overviewTask).get();

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      final SrTopicArchivePageTask archiveTask =
          new SrTopicArchivePageTask(this, shows, jsoupConnection);
      final Queue<SrTopicUrlDTO> filmDtos = new ConcurrentLinkedQueue<>();
      filmDtos.addAll(forkJoinPool.submit(archiveTask).get());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmDtos.size());
      getAndSetMaxCount(filmDtos.size());

      return new SrFilmDetailTask(this, filmDtos, jsoupConnection);
    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in SR crawler.", ex);
    }
    return null;
  }
}
