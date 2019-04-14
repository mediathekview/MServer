/*
 * CrawlerUrlType.java
 *
 * Projekt    : MServer
 * erstellt am: 19.11.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.base.config;

import org.apache.commons.lang3.StringUtils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

public enum CrawlerUrlType {
  BR_API_URL("https://proxy-base.master.mango.express/graphql"),
  FUNK_API_URL("https://www.funk.net/api/v4.0/");

  private URL defaultUrl;

  CrawlerUrlType(final String urlText) {
    try {
      if (StringUtils.isNotEmpty(urlText)) {
        this.defaultUrl = new URL(urlText);
      }
    } catch (final MalformedURLException e) {
      this.defaultUrl = null;
    }
  }

  public Optional<URL> getDefaultUrl() {
    return Optional.ofNullable(defaultUrl);
  }
}
