package de.mediathekview.mserver.crawler.swr;

import de.mediathekview.mserver.base.utils.UrlUtils;

public class SwrUrlOptimizer {

  public static final String SWR_URL_1280 = ".xl.mp4";
  public static final String SWR_URL_1920 = ".xxl.mp4";

  public String optimizeHdUrl(final String url) {
    if (url.contains(SWR_URL_1280)) {
      String optimizedUrl = url.replace(SWR_URL_1280, SWR_URL_1920);
      if (UrlUtils.existsUrl(optimizedUrl)) {
        return optimizedUrl;
      }
    }

    return url;
  }
}
