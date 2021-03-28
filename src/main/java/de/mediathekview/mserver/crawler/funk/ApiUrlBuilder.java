package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.net.URL;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApiUrlBuilder {
  private final String urlTemplate;
  private final CrawlerUrlType baseUrlUrlType;
  private final List<String> parameters;
  private final MServerConfigDTO config;

  public ApiUrlBuilder(
      final CrawlerUrlType baseUrlUrlType,
      final String urlTemplate,
      final MServerConfigDTO config) {
    super();
    parameters = new ArrayList<>();
    this.baseUrlUrlType = baseUrlUrlType;
    this.urlTemplate = urlTemplate;
    this.config = config;
  }

  public ApiUrlBuilder withParameter(final String parameter) {
    parameters.add(parameter);
    return this;
  }

  public String asString() {
    final Optional<URL> apiUrl = config.getSingleCrawlerURL(baseUrlUrlType);
    if (apiUrl.isPresent()) {
      final List<String> urlParameter = new ArrayList<>();
      urlParameter.add(String.valueOf(apiUrl.get()));
      urlParameter.addAll(parameters);
      return String.format(urlTemplate, urlParameter.toArray());
    } else {
      throw new IllegalStateException("The API base URL is empty!");
    }
  }

  public CrawlerUrlDTO asCrawlerUrl() {
    return new CrawlerUrlDTO(asString());
  }

  public Queue<CrawlerUrlDTO> asQueue() {
    return new ConcurrentLinkedQueue<>(Collections.singletonList(asCrawlerUrl()));
  }
}
