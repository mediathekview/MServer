package mServer.crawler.sender.funk;

import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ApiUrlBuilder {
  private final String urlTemplate;
  private final CrawlerUrlType baseUrlUrlType;
  private final List<String> parameters;

  public ApiUrlBuilder(
          final CrawlerUrlType baseUrlUrlType,
          final String urlTemplate) {
    super();
    parameters = new ArrayList<>();
    this.baseUrlUrlType = baseUrlUrlType;
    this.urlTemplate = urlTemplate;
  }

  public ApiUrlBuilder withParameter(final String parameter) {
    parameters.add(parameter);
    return this;
  }

  public String asString() {
    final Optional<URL> apiUrl = baseUrlUrlType.getDefaultUrl();
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

  public ConcurrentLinkedQueue<CrawlerUrlDTO> asQueue() {
    return new ConcurrentLinkedQueue<>(Collections.singletonList(asCrawlerUrl()));
  }
}
