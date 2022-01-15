package mServer.crawler.sender.funk;

import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public enum FunkApiUrls {
  /**
   * The channels overview url. No channel id needed.
   */
  CHANNELS("%s/channels/?size=%s"),
  /**
   * The video overview url. No channel id needed.
   */
  VIDEOS("%s/videos/?size=%s"),
  /**
   * The videos for a specific channel. Channel id needed.
   */
  VIDEOS_BY_CHANNEL("%s/videos/byChannelId/%s?size=%s");

  private static final String MAX_URLS_PER_TASK = "99";
  private final String urlTemplate;

  FunkApiUrls(final String aUrlTemplate) {
    urlTemplate = aUrlTemplate;
  }

  public CrawlerUrlDTO getAsCrawlerUrl(
          final @Nullable String channelId) {
    return buildUrl(channelId).asCrawlerUrl();
  }

  @NotNull
  private ApiUrlBuilder buildUrl(final @Nullable String channelId) {
    final ApiUrlBuilder apiUrlBuilder =
            new ApiUrlBuilder(CrawlerUrlType.FUNK_API_URL, urlTemplate);
    Optional.ofNullable(channelId).ifPresent(apiUrlBuilder::withParameter);
    apiUrlBuilder.withParameter(MAX_URLS_PER_TASK);
    return apiUrlBuilder;
  }

  public ConcurrentLinkedQueue<CrawlerUrlDTO> getAsQueue() {
    return getAsQueue(null);
  }

  public ConcurrentLinkedQueue<CrawlerUrlDTO> getAsQueue(
          final @Nullable String channelId) {
    return buildUrl(channelId).asQueue();
  }
}
