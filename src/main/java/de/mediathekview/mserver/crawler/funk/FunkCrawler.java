package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.funk.tasks.FunkChannelTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class FunkCrawler extends AbstractCrawler {
  private static final String AUTH_KEY =
      "1efb06afc842521f5693b5ce4e5b6c4530ce4ea8c1c09ed618f91da39c11da92";

  public FunkCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.FUNK;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    /*
    Three phases:
    1. Get all channels create FunkChannelDTOs of them
    2. Parallel:
    2.1. Get all videos for channel
    2.2. Get latest videos. Use channels to retrieve channel title as Thema.
     */
    final Set<FunkChannelDTO> funkChannels;
    final Object featureFunkChannels = forkJoinPool.submit(new FunkChannelTask(this));

    return null;
  }
}
