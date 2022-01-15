package mServer.crawler.sender.funk;

import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.jetbrains.annotations.NotNull;

import java.util.Queue;
import java.util.stream.Stream;

public enum FunkUrls {
  /**
   * [website]/channel/[channelAlias]/[alias]
   */
  WEBSITE(CrawlerUrlType.FUNK_WEBSITE, "%s/channel/%s/%s"),
  /**
   * [ApiBaseUrl]/session/init
   */
  NEXX_CLOUD_SESSION_INIT(CrawlerUrlType.NEXX_CLOUD_API_URL, "%s/session/init"),
  /**
   * [ApiBaseUrl]/videos/byid/[videoId]
   */
  NEXX_CLOUD_VIDEO(CrawlerUrlType.NEXX_CLOUD_API_URL, "%s/videos/byid/%s");

  private final String urlTemplate;
  private final CrawlerUrlType baseUrlUrlType;

  FunkUrls(final CrawlerUrlType aBaseUrlUrlType, final String aUrlTemplate) {
    baseUrlUrlType = aBaseUrlUrlType;
    urlTemplate = aUrlTemplate;
  }

  @NotNull
  private ApiUrlBuilder buildUrl(final String... parameters) {
    final ApiUrlBuilder apiUrlBuilder = new ApiUrlBuilder(baseUrlUrlType, urlTemplate);
    Stream.of(parameters).forEachOrdered(apiUrlBuilder::withParameter);
    return apiUrlBuilder;
  }

  public Queue<CrawlerUrlDTO> getAsQueue(final String... parameters) {
    return buildUrl(parameters).asQueue();
  }

  public String getAsString(final String... parameters) {
    return buildUrl(parameters).asString();
  }
}
