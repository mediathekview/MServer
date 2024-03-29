package mServer.crawler.sender.funk;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public enum CrawlerUrlType {
  FUNK_WEBSITE("https://www.funk.net"),
  FUNK_API_URL("https://www.funk.net/api/v4.0/"),
  NEXX_CLOUD_API_URL("https://api.nexx.cloud/v3/741");

  private URL defaultUrl;

  CrawlerUrlType(final String urlText) {
    try {
      if (StringUtils.isNotEmpty(urlText)) {
        defaultUrl = new URL(urlText);
      }
    } catch (final MalformedURLException e) {
      defaultUrl = null;
    }
  }

  public Optional<URL> getDefaultUrl() {
    return Optional.ofNullable(defaultUrl);
  }
}
