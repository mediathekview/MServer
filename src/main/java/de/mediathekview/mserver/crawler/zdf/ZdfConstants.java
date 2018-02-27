package de.mediathekview.mserver.crawler.zdf;

public final class ZdfConstants {
  private ZdfConstants() {}

  public static final String URL_BASE = "https://www.zdf.de";
  
  /**
   * URL für die Sendungen eines Tages.
   * Als Parameter muss das Datum als yyyy-mm-dd angegeben werden
   */
  public static final String URL_DAY = URL_BASE + "/sendung-verpasst?airtimeDate=";
  
  /**
   * URL für die Sendungen nach Buchstaben.
   */
  public static final String URL_LETTER_PAGE = URL_BASE + "/sendungen-a-z";
}
