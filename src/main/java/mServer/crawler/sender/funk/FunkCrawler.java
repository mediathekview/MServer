package mServer.crawler.sender.funk;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.Log;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.MediathekCrawler;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.funk.json.FunkChannelDeserializer;
import mServer.crawler.sender.funk.json.FunkVideoDeserializer;
import mServer.crawler.sender.funk.tasks.FunkChannelsRestTask;
import mServer.crawler.sender.funk.tasks.FunkRestEndpoint;
import mServer.crawler.sender.funk.tasks.FunkRestTask;
import mServer.crawler.sender.funk.tasks.FunkVideosToFilmsTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;
import java.util.stream.Collectors;

public class FunkCrawler extends MediathekCrawler {
  public FunkCrawler(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, "Funk.net", 0, 1, startPrio);
  }

  @Override
  protected RecursiveTask<Set<DatenFilm>> createCrawlerTask() {

    final ConcurrentLinkedQueue<FilmInfoDto> filmInfos = new ConcurrentLinkedQueue<>();
    Map<String, FunkChannelDTO> channels = new HashMap<>();
    try {
      final ForkJoinTask<Set<FunkChannelDTO>> featureFunkChannels = createChannelTask();
      channels = featureFunkChannels.get().stream()
              .collect(Collectors.toMap(FunkChannelDTO::getChannelId, Function.identity()));
      Log.sysLog("Funk Channels: " + channels.size());

      final ForkJoinTask<Set<FilmInfoDto>> featureLatestVideos = createLatestVideosTask();
      filmInfos.addAll(featureLatestVideos.get());
      Log.sysLog("Funk aktuelle Videos: " + filmInfos.size());

      if (CrawlerTool.loadLongMax()) {

        final ConcurrentLinkedQueue<CrawlerUrlDTO> funkVideosByChannelUrls =
                convertChannelsToVideosByChannelUrls(new HashSet<>(channels.values()));
        final ForkJoinTask<Set<FilmInfoDto>> featureChannelVideos =
                createChannelVideos(funkVideosByChannelUrls);
        filmInfos.addAll(featureChannelVideos.get());
      }

    } catch (InterruptedException | ExecutionException exception) {
      Log.errorLog(56146547, exception);
    }
    Log.sysLog("Funk Anzahl: " + filmInfos.size());
    meldungAddMax(filmInfos.size());

    return new FunkVideosToFilmsTask(this, filmInfos, channels, Optional.empty());
  }

  private ForkJoinTask<Set<FilmInfoDto>> createChannelVideos(
          final ConcurrentLinkedQueue<CrawlerUrlDTO> funkVideosByChannelUrls) {
    return forkJoinPool.submit(
            new FunkRestTask<>(
                    this,
                    new FunkRestEndpoint<>(
                            FunkApiUrls.VIDEOS_BY_CHANNEL, new FunkVideoDeserializer()),
                    funkVideosByChannelUrls));
  }

  private ForkJoinTask<Set<FilmInfoDto>> createLatestVideosTask() {
    return forkJoinPool.submit(
            new FunkRestTask<>(
                    this,
                    new FunkRestEndpoint<>(
                            FunkApiUrls.VIDEOS, new FunkVideoDeserializer())));
  }

  private ForkJoinTask<Set<FunkChannelDTO>> createChannelTask() {
      return forkJoinPool.submit(
            new FunkChannelsRestTask(
                    this,
                    new FunkRestEndpoint<>(
                            FunkApiUrls.CHANNELS, new FunkChannelDeserializer())));
  }

  @NotNull
  private ConcurrentLinkedQueue<CrawlerUrlDTO> convertChannelsToVideosByChannelUrls(
          final Set<FunkChannelDTO> funkChannels) {
    return funkChannels.parallelStream()
            .map(channel -> FunkApiUrls.VIDEOS_BY_CHANNEL.getAsCrawlerUrl(channel.getChannelId()))
            .collect(Collectors.toCollection(ConcurrentLinkedQueue::new));
  }
}
