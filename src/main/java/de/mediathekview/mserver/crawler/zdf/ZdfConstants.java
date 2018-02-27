package de.mediathekview.mserver.crawler.zdf;

public final class ZdfConstants {
  private ZdfConstants() {}

  /**
   * Base url of the ZDF website
   */
  public static final String URL_BASE = "https://www.zdf.de";

  /**
   * Base url of the ZDF api
   */
  public static final String URL_API_BASE = "https://api.zdf.de";

  /**
   * Url to search the films
   */
  public static final String URL_DAY = URL_API_BASE + "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=2018-02-24T12:00:00.000%2B01:00&to=2018-02-24T13:00:00.878%2B01:00&sortBy=date&page=1";
}
