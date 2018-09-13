package de.mediathekview.mserver.crawler.swr;

public final class SwrConstants {

  /**
   * Base url of the SWR mediathek.
   */
  public static final String URL_BASE = "https://swrmediathek.de";

  /**
   * Url of the film website.
   * Id of the film has to be added at the end
   */
  public static final String URL_FILM_PAGE = URL_BASE + "/player.htm?show=";

  private SwrConstants() {
  }
}
