package mServer.crawler.sender.orf;

import java.net.URL;

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
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CrawlerUrlDTO other = (CrawlerUrlDTO) obj;
    if (url == null) {
      if (other.url != null) {
        return false;
      }
    } else if (!url.equals(other.url)) {
      return false;
    }
    return true;
  }

  public String getUrl() {
    return url;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (url == null ? 0 : url.hashCode());
    return result;
  }

  public void setUrl(final String aUrl) {
    url = aUrl;
    if (url.startsWith("//")) {
      url = HTTPS + url;
    }
  }

}
