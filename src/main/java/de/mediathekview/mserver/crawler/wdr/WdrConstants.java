package de.mediathekview.mserver.crawler.wdr;

public final class WdrConstants {
  private WdrConstants() {}

  public static final String URL_BASE = "https://www1.wdr.de";
  
  /**
   * URL für die Sendungen eines Tages
   * Als Parameter muss das Datum als ddMMyyyy angegeben werden
   */
  public static final String URL_DAY = URL_BASE + "/mediathek/video/sendungverpasst/sendung-verpasst-100~_tag-%s.html";
}
