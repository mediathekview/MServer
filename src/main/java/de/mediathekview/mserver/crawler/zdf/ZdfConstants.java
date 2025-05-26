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

  public static final String NO_CURSOR = "null";
  public static final int EPISODES_PAGE_SIZE = 24;

  // todo: itemsfilter so ok?
  public static final String URL_LETTER_PAGE =
      URL_API_BASE
          + "/graphql?operationName=specialPageByCanonical&" +
              "variables=%s&" +
              "extensions=%s";
  public static final String URL_LETTER_PAGE_VARIABLES =
      "{\"staticGridClusterPageSize\":6,\"staticGridClusterOffset\":0,\"canonical\":\"sendungen-100\",\"endCursor\":%s,\"tabIndex\":%d,\"itemsFilter\":{\"teaserUsageNotIn\":[\"TIVI_HBBTV_ONLY\"]}}";
  public static final String URL_LETTER_PAGE_EXTENSIONS =
      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"7d33167e7700ba57779f48b28b5d485c8ada0a1d5dfbdc8a261b7bd62ca28ba7\"}}";

  // todo filter raus => ok? "filterBy":{"idIn":["13-fragen-_season_1"]}
  public static final String URL_TOPIC_PAGE = URL_API_BASE + "/graphql?operationName=seasonByCanonical&" +
          "variables=%s&" +
          "extensions=%s";
  public static final String URL_TOPIC_PAGE_VARIABLES = "{\"seasonIndex\":%d,\"episodesPageSize\":%d,\"canonical\":\"%s\",\"sortBy\":[{\"field\":\"EDITORIAL_DATE\",\"direction\":\"DESC\"}]}";
  public static final String URL_TOPIC_PAGE_VARIABLES_WITH_CURSOR = "{\"seasonIndex\":%d,\"episodesPageSize\":%d,\"canonical\":\"%s\",\"sortBy\":[{\"field\":\"EDITORIAL_DATE\",\"direction\":\"DESC\"}],\"episodesAfter\":\"%s\"}";
  public static final String URL_TOPIC_PAGE_EXTENSIONS =
      "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"9412a0f4ac55dc37d46975d461ec64bfd14380d815df843a1492348f77b5c99a\"}}";

  public static final String URL_FILM_ENRY =
          URL_API_BASE + "/graphql?operationName=GetVideoMetaByCanonical&"
                  + "variables={\"canonical\"=\"%s\"}&"
                  + "extensions={\"persistedQuery\"={\"version\"=1,\"sha256Hash\"=\"737eb4421d274259baa3051929f4ecfef2d2afc59f12a9d82285c14dbdd1dd0d\"}}";

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
    PARTNER_TO_SENDER.put("ZDFinfo", Sender.ZDF_INFO);
    PARTNER_TO_SENDER.put("ZDFneo", Sender.ZDF_NEO);
    PARTNER_TO_SENDER.put("ZDF", Sender.ZDF); 
    PARTNER_TO_SENDER.put("EMPTY", Sender.ZDF);
    // IGNORED Sender [KI.KA, WDR, PHOENIX, one, HR, 3sat, SWR, arte, BR, RBB, ARD, daserste, alpha, MDR, radiobremen, funk, ZDF, NDR, SR]
  }

  private ZdfConstants() {}
}
