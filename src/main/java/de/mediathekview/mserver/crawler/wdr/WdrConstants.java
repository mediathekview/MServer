package de.mediathekview.mserver.crawler.wdr;

public final class WdrConstants {
  private WdrConstants() {}

  public static final String URL_BASE = "https://www1.wdr.de";
  
  /**
   * URL für die Sendungen eines Tages
   * Als Parameter muss das Datum als ddMMyyyy angegeben werden
   */
  public static final String URL_DAY = URL_BASE + "/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-%s.html";
  
  /**
   * URL für die Sendungen nach Buchstaben
   */
  public static final String URL_LETTER_PAGE = URL_BASE + "/mediathek/video/sendungen-a-z/index.html";

  /**
   * URL für Videos von WDR4
   */
  public static final String URL_RADIO_WDR4 = URL_BASE + "/mediathek/video/radio/wdr4/index.html";

  /**
   * URL für Videos von WDR5
   */
  public static final String URL_RADIO_WDR5 = URL_BASE + "/mediathek/video/radio/wdr5/index.html";
}
