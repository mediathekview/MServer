package de.mediathekview.mserver.crawler.dreisat;

public final class DreisatConstants {

  /** Base url of the 3Sat website. */
  public static final String URL_BASE = "https://www.3sat.de";
  /** Base url of the 3Sat api. */
  public static final String URL_API_BASE = "https://api.3sat.de";
  /** Url to search the films. */
  public static final String URL_DAY =
      URL_API_BASE
          + "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=%sT00:00:00.000%%2B01:00&to=%sT23:59:59.999%%2B01:00&sortBy=date&page=1";

  private DreisatConstants() {}
}
