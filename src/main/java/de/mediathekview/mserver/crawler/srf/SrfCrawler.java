package de.mediathekview.mserver.crawler.srf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.srf.tasks.SrfFilmDetailTask;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTopicOverviewTask;
import de.mediathekview.mserver.crawler.srf.tasks.SrfTopicsOverviewTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class SrfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(SrfCrawler.class);

  public SrfCrawler(
          final ForkJoinPool aForkJoinPool,
          final Collection<MessageListener> aMessageListeners,
          final Collection<SenderProgressListener> aProgressListeners,
          final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.SRF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      final ConcurrentLinkedQueue<CrawlerUrlDTO> topicsUrls = new ConcurrentLinkedQueue<>();
      topicsUrls.add(new CrawlerUrlDTO(SrfConstants.OVERVIEW_PAGE_URL));
      final SrfTopicsOverviewTask overviewTask = new SrfTopicsOverviewTask(this, topicsUrls);
      final ConcurrentLinkedQueue<TopicUrlDTO> topicUrls = new ConcurrentLinkedQueue<>(forkJoinPool.submit(overviewTask).get());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), topicUrls.size());

      final SrfTopicOverviewTask task = new SrfTopicOverviewTask(this, topicUrls, SrfConstants.BASE_URL);
      forkJoinPool.execute(task);

      final ConcurrentLinkedQueue<CrawlerUrlDTO> dtos = new ConcurrentLinkedQueue<>();
      dtos.addAll(task.join());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), dtos.size());
      getAndSetMaxCount(dtos.size());

      return new SrfFilmDetailTask(this, dtos);

    } catch (final InterruptedException | ExecutionException ex) {
      LOG.fatal("Exception in SRF crawler.", ex);
    }
    return null;
  }
}
