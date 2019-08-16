package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.base.utils.UrlUtils;

public class ArdUrlOptimizer {

  public static final String ARD_URL_1280 = ".xl.mp4";
  public static final String ARD_URL_1920 = ".xxl.mp4";

  public String optimizeHdUrl(final String url) {
    if (url.contains(ARD_URL_1280)) {
      final String optimizedUrl = url.replace(ARD_URL_1280, ARD_URL_1920);
      if (UrlUtils.existsUrl(optimizedUrl)) {
        return optimizedUrl;
      }
    }

    return url;
  }
}
