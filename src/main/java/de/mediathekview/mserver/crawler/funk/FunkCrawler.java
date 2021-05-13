package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
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

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Function;
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

    final Queue<FilmInfoDto> filmInfos = new ConcurrentLinkedQueue<>();

    try {
      final ForkJoinTask<Set<FilmInfoDto>> featureLatestVideos = createLatestVideosTask();
      final ForkJoinTask<Set<FunkChannelDTO>> featureFunkChannels = createChannelTask();
      final Map<String, FunkChannelDTO> channels =
              featureFunkChannels.get().stream()
                      .collect(Collectors.toMap(FunkChannelDTO::getChannelId, Function.identity()));

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        final Queue<CrawlerUrlDTO> funkVideosByChannelUrls =
                convertChannelsToVideosByChannelUrls(new HashSet<>(channels.values()));
        final ForkJoinTask<Set<FilmInfoDto>> featureChannelVideos =
                createChannelVideos(funkVideosByChannelUrls);
        filmInfos.addAll(featureChannelVideos.get());
      }

      filmInfos.addAll(featureLatestVideos.get());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), filmInfos.size());
      getAndSetMaxCount(filmInfos.size());

      return new FunkVideosToFilmsTask(this, filmInfos, channels, null);
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
      final Queue<CrawlerUrlDTO> funkVideosByChannelUrls) {
    return forkJoinPool.submit(
        new FunkRestTask<>(
            this,
            new FunkRestEndpoint<>(
                FunkApiUrls.VIDEOS_BY_CHANNEL, new FunkVideoDeserializer(this, crawlerConfig)),
            funkVideosByChannelUrls));
  }

  private ForkJoinTask<Set<FilmInfoDto>> createLatestVideosTask() {
    return forkJoinPool.submit(
        new FunkRestTask<>(
            this,
            new FunkRestEndpoint<>(
                FunkApiUrls.VIDEOS, new FunkVideoDeserializer(this, crawlerConfig))));
  }

  private ForkJoinTask<Set<FunkChannelDTO>> createChannelTask() {
    return forkJoinPool.submit(
        new FunkRestTask<>(
            this,
            new FunkRestEndpoint<>(
                FunkApiUrls.CHANNELS, new FunkChannelDeserializer(crawlerConfig))));
  }

  @NotNull
  private Queue<CrawlerUrlDTO> convertChannelsToVideosByChannelUrls(
      final Set<FunkChannelDTO> funkChannels) {
    return funkChannels.parallelStream()
        .map(channel -> FunkApiUrls.VIDEOS_BY_CHANNEL.getAsCrawlerUrl(this, channel.getChannelId()))
        .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
  }
}
