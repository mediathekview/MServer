package de.mediathekview.mserver.crawler.zdf;

import java.util.HashMap;
import java.util.Map;

import de.mediathekview.mlib.daten.Sender;

public final class ZdfConstants {

  /** Name of the header required for authentification. */
  public static final String HEADER_AUTHENTIFICATION = "Api-Auth";
  /** Base url of the ZDF website. */
  public static final String URL_BASE = "https://www.zdf.de";
  public static final String URL_HTML_DAY = URL_BASE + "/sendung-verpasst?airtimeDate=%s";
  public static final String URL_TOPICS = URL_BASE + "/sendungen-a-z";
  /** Base url of the ZDF api. */
  public static final String URL_API_BASE = "https://api.zdf.de";
  /** Url to search the films. */
  public static final String URL_DAY =
      URL_API_BASE
          + "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=%sT00:00:00.000%%2B01:00&to=%sT23:59:59.999%%2B01:00&sortBy=date&page=1";
  /** Url to request film details */
  public static final String URL_FILM_JSON = "%s/content/documents/%s.json";
  public static final String LANGUAGE_SUFFIX_AD = "-ad";
  public static final String LANGUAGE_SUFFIX_DGS = "-dgs";
  /** The language key of english. */
  public static final String LANGUAGE_ENGLISH = "eng";
  /** The language key of french. */
  public static final String LANGUAGE_FRENCH = "fra";
  /** The language key of german. */
  public static final String LANGUAGE_GERMAN = "deu";
  /** The language key of german audio description. */
  public static final String LANGUAGE_GERMAN_AD = LANGUAGE_GERMAN + LANGUAGE_SUFFIX_AD;
  public static final String LANGUAGE_GERMAN_DGS = LANGUAGE_GERMAN + LANGUAGE_SUFFIX_DGS;
  
  public static final Map<String, Sender> PARTNER_TO_SENDER = new HashMap<>();

  static {
    PARTNER_TO_SENDER.put("ZDFinfo", Sender.ZDF);
    PARTNER_TO_SENDER.put("ZDFneo", Sender.ZDF);
    PARTNER_TO_SENDER.put("ZDF", Sender.ZDF); 
    PARTNER_TO_SENDER.put("EMPTY", Sender.ZDF);
    // IGNORED Sender [KI.KA, WDR, PHOENIX, one, HR, 3sat, SWR, arte, BR, RBB, ARD, daserste, alpha, MDR, radiobremen, funk, ZDF, NDR, SR]
  }

  private ZdfConstants() {}
}
