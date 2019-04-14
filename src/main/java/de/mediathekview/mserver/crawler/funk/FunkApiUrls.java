package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public enum FunkApiUrls {
  CHANNELS("%s/channels/?size=%d"),
  VIDEOS("%s/videos/?size=%d"),
  VIDEOS_BY_CHANNEL("%s/videos/byChannelId/%d?size=%d");

  private final String urlTemplate;

  FunkApiUrls(final String aUrlTemplate) {
    urlTemplate = aUrlTemplate;
  }

  public CrawlerUrlDTO getAsCrawlerUrl(final AbstractCrawler crawler) {
    return getAsCrawlerUrl(crawler, Optional.empty());
  }

  public CrawlerUrlDTO getAsCrawlerUrl(
      final AbstractCrawler crawler, final Optional<String> channelId) {
    final Optional<URL> apiUrl =
        crawler.getRuntimeConfig().getSingleCrawlerURL(CrawlerUrlType.FUNK_API_URL);
    if (apiUrl.isPresent()) {
      final List<String> parameter = new ArrayList<>();
      parameter.add(String.valueOf(apiUrl.get()));
      channelId.ifPresent(parameter::add);
      parameter.add(String.valueOf(crawler.getCrawlerConfig().getMaximumUrlsPerTask()));

      return new CrawlerUrlDTO(String.format(urlTemplate, parameter.toArray()));
    } else {
      throw new IllegalStateException("The Funk API base URL is empty!");
    }
  }

  public ConcurrentLinkedQueue<CrawlerUrlDTO> getAsQueue(final AbstractCrawler crawler) {
    return getAsQueue(crawler, Optional.empty());
  }

  public ConcurrentLinkedQueue<CrawlerUrlDTO> getAsQueue(
      final AbstractCrawler crawler, final Optional<String> channelId) {
    return new ConcurrentLinkedQueue<>(
        Collections.singletonList(getAsCrawlerUrl(crawler, channelId)));
  }
}
