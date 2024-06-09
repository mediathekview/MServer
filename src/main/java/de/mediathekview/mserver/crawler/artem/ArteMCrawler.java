package de.mediathekview.mserver.crawler.artem;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
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

public class ArteMCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(ArteMCrawler.class);

  public ArteMCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARTE_DE;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    try {
      // get all brands from json doc
      final Queue<CrawlerUrlDTO> root = new ConcurrentLinkedQueue<>();
      root.add(new CrawlerUrlDTO(ArteMConstants.ALL_VIDEOS));
      final ArteMVideoTask arteMVideoTask = new ArteMVideoTask(this, root, ArteMConstants.AUTH, 0);
      final Queue<ArteMVideoDto> videos = new ConcurrentLinkedQueue<>();
      videos.addAll(arteMVideoTask.fork().join());
      //
      printMessage(ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), videos.size());
      getAndSetMaxCount(videos.size());
      //
      return new ArteMStreamTask(this, videos, ArteMConstants.AUTH, 0);
    } catch (final Exception ex) {
      LOG.fatal("Exception in ARTE_DE crawler.", ex);
    }

    return null;
  }

}
