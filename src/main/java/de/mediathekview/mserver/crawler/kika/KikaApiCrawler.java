package de.mediathekview.mserver.crawler.kika;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.kika.json.KikaApiFilmDto;
import de.mediathekview.mserver.crawler.kika.tasks.*;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class KikaApiCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(KikaApiCrawler.class);

  public KikaApiCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.KIKA;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      // get all brands from json doc
      final Queue<TopicUrlDTO> root = new ConcurrentLinkedQueue<>();
      root.add(new TopicUrlDTO("all videos",KikaApiConstants.ALL_VIDEOS));
      final KikaApiTopicTask aKikaApiTopicOverviewTask = new KikaApiTopicTask(this, root, 0);
      final Queue<KikaApiFilmDto> videos = new ConcurrentLinkedQueue<>();
      videos.addAll(aKikaApiTopicOverviewTask.fork().join());
      //
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), videos.size());
      getAndSetMaxCount(videos.size());
      //
      // get all video urls for this episode
      return new KikaApiFilmTask(this, videos);
    } catch (final Exception ex) {
      LOG.fatal("Exception in KIKA crawler.", ex);
    }

    return null;
  }

}
