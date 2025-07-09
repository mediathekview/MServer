package mServer.crawler.sender.dreisat;

import de.mediathekview.mlib.Const;

import java.util.HashMap;
import java.util.Map;

public final class DreisatConstants {

  /**
   * Base url of the 3Sat website.
   */
  public static final String URL_BASE = "https://www.3sat.de";
  /**
   * Base url of the 3Sat api.
   */
  public static final String URL_API_BASE = "https://api.3sat.de";
  /**
   * Url to search the films.
   */
  public static final String URL_DAY
          = URL_API_BASE
          + "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=%sT00:00:00.000%%2B01:00&to=%sT23:59:59.999%%2B01:00&sortBy=date&page=1";

  public static final String URL_HTML_DAY = URL_BASE + "/programm?airtimeDate=%s";

  public static final Map<String, String> PARTNER_TO_SENDER = new HashMap<>();
  static {
    PARTNER_TO_SENDER.put("3sat", Const.DREISAT);
  }

  private DreisatConstants() {
  }
}
