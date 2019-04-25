package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public enum FunkApiUrls {
  /** The channels overview url. No channel id needed. */
  CHANNELS("%s/channels/?size=%s"),
  /** The video overview url. No channel id needed. */
  VIDEOS("%s/videos/?size=%s"),
  /** The videos for a specific channel. Channel id needed. */
  VIDEOS_BY_CHANNEL("%s/videos/byChannelId/%s?size=%s");

  private final String urlTemplate;

  FunkApiUrls(final String aUrlTemplate) {
    urlTemplate = aUrlTemplate;
  }

  public CrawlerUrlDTO getAsCrawlerUrl(
      final AbstractCrawler crawler, final Optional<String> channelId) {
    return buildUrl(crawler, channelId).asCrawlerUrl();
  }

  @NotNull
  private ApiUrlBuilder buildUrl(final AbstractCrawler crawler, final Optional<String> channelId) {
    final ApiUrlBuilder apiUrlBuilder = new ApiUrlBuilder(CrawlerUrlType.FUNK_API_URL, urlTemplate);
    channelId.ifPresent(apiUrlBuilder::withParameter);
    apiUrlBuilder.withParameter(String.valueOf(crawler.getCrawlerConfig().getMaximumUrlsPerTask()));
    return apiUrlBuilder;
  }

  public ConcurrentLinkedQueue<CrawlerUrlDTO> getAsQueue(final AbstractCrawler crawler) {
    return getAsQueue(crawler, Optional.empty());
  }

  public ConcurrentLinkedQueue<CrawlerUrlDTO> getAsQueue(
      final AbstractCrawler crawler, final Optional<String> channelId) {
    return buildUrl(crawler, channelId).asQueue();
  }
}
