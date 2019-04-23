package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.funk.json.FunkChannelDeserializer;
import de.mediathekview.mserver.crawler.funk.json.FunkVideoDeserializer;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestEndpoint;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestTask;
import de.mediathekview.mserver.crawler.funk.tasks.FunkVideosToFilmsTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class FunkCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(FunkCrawler.class);

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
    final ForkJoinTask<Set<FunkChannelDTO>> featureFunkChannels = createChannelTask();

    final ForkJoinTask<Set<FilmInfoDto>> featureLatestVideos = createLatestVideosTask();

    try {
      final Set<FunkChannelDTO> funkChannels = featureFunkChannels.get();

      final ConcurrentLinkedQueue<CrawlerUrlDTO> funkVideosByChannelUrls =
          convertChannelsToVideosByChannelUrls(funkChannels);

      final ForkJoinTask<Set<FilmInfoDto>> featureChannelVideos =
          createChannelVideos(funkVideosByChannelUrls);

      final ConcurrentLinkedQueue<FilmInfoDto> filmInfos =
          new ConcurrentLinkedQueue<>(featureLatestVideos.get());
      filmInfos.addAll(featureChannelVideos.get());

      // TODO filmInfo url to a real nexx cloud url.

      return new FunkVideosToFilmsTask(this, filmInfos, Optional.empty());
    } catch (final InterruptedException interruptedException) {
      printErrorMessage();
      LOG.debug("Funk got interrupted.", interruptedException);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException executionException) {
      printErrorMessage();
      LOG.fatal(
          "Something really bad happened while gathering the Funk channels.", executionException);
    }

    return null;
  }

  private ForkJoinTask<Set<FilmInfoDto>> createChannelVideos(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> funkVideosByChannelUrls) {
    return forkJoinPool.submit(
        new FunkRestTask<>(
            this,
            new FunkRestEndpoint<>(
                FunkApiUrls.VIDEOS_BY_CHANNEL, new FunkVideoDeserializer(Optional.of(this))),
            funkVideosByChannelUrls));
  }

  private ForkJoinTask<Set<FilmInfoDto>> createLatestVideosTask() {
    return forkJoinPool.submit(
        new FunkRestTask<>(
            this,
            new FunkRestEndpoint<>(
                FunkApiUrls.VIDEOS, new FunkVideoDeserializer(Optional.of(this)))));
  }

  private ForkJoinTask<Set<FunkChannelDTO>> createChannelTask() {
    return forkJoinPool.submit(
        new FunkRestTask<>(
            this, new FunkRestEndpoint<>(FunkApiUrls.CHANNELS, new FunkChannelDeserializer())));
  }

  @NotNull
  private ConcurrentLinkedQueue<CrawlerUrlDTO> convertChannelsToVideosByChannelUrls(
      final Set<FunkChannelDTO> funkChannels) {
    return funkChannels
        .parallelStream()
        .map(
            channel ->
                FunkApiUrls.VIDEOS_BY_CHANNEL.getAsCrawlerUrl(
                    this, Optional.of(channel.getChannelId())))
        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
  }
}
