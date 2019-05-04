package de.mediathekview.mserver.crawler.basic;

import java.net.URL;
import java.util.Objects;

public class CrawlerUrlDTO {
  protected static final String HTTPS = "https:";
  private String url;

  public CrawlerUrlDTO(final String aUrl) {
    setUrl(aUrl);
  }

  public CrawlerUrlDTO(final URL aUrl) {
    this(aUrl.toString());
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final CrawlerUrlDTO that = (CrawlerUrlDTO) o;
    return Objects.equals(url, that.url);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url);
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(final String aUrl) {
    url = aUrl;
    if (url.startsWith("//")) {
      url = HTTPS + url;
    }
  }
}
