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
import java.net.URI;
import java.net.URL;
import java.util.Optional;

public enum CrawlerUrlType {
  ;

  private URL defaultUrl;

  CrawlerUrlType(final String urlText) {
    try {
      if (StringUtils.isNotEmpty(urlText)) {
        defaultUrl = URI.create(urlText).toURL();
      }
    } catch (final MalformedURLException e) {
      defaultUrl = null;
    }
  }

  public Optional<URL> getDefaultUrl() {
    return Optional.ofNullable(defaultUrl);
  }
}
