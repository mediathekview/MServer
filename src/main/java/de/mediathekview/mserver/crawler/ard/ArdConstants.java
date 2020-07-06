package de.mediathekview.mserver.crawler.ard;

public class ArdConstants {

  public static final String API_URL = "https://api.ardmediathek.de";
  public static final String BASE_URL = "https://api.ardmediathek.de/public-gateway";

  public static final String ITEM_URL = API_URL + "/page-gateway/pages/ard/item/";

  public static final String TOPICS_URL = API_URL + "/page-gateway/pages/%s/shows/";
  public static final String TOPIC_URL = API_URL + "/page-gateway/widgets/ard/asset/%s?pageSize=50";
  public static final String DAY_PAGE_URL = API_URL + "/page-gateway/compilations/%s/pastbroadcasts?startDateTime=%sT00:00:00.000Z&endDateTime=%sT23:59:59.000Z&pageNumber=0&pageSize=100";

  public static final String DEFAULT_CLIENT = "ard";
  public static final String DEFAULT_DEVICE = "pc";

  public static final String[] CLIENTS =
      new String[] {
        "daserste",
        "br",
        "hr",
        "mdr",
        "ndr",
        "radiobremen",
        "rbb",
        "sr",
        "swr",
        "wdr",
        "one",
        "alpha",
        "tagesschau24"
      };

  public static final int QUERY_DAY_SEARCH_VERSION = 1;
  public static final String QUERY_DAY_SEARCH_HASH =
      "a62c5366a603ad60a46e4b0dd0f52b1855d401026016a639bbbfbe88e2118989";

  public static final int QUERY_FILM_VERSION = 1;
  public static final String QUERY_FILM_HASH =
      "38e4c23d15b4b007e2e31068658944f19797c2fb7a75c93bc0a77fe1632476c6";

  public static final int QUERY_TOPICS_VERSION = 1;
  public static final String QUERY_TOPICS_HASH =
      "fdbab76da7d6aeb1ae859e1758dd1db068824dbf1623c02bc4c5f61facb474c2";

  public static final int QUERY_TOPIC_VERSION = 1;
  public static final String QUERY_TOPIC_HASH =
      "1801f782ce062a81d19465b059e6147671da882c510cca99e9a9ade8e542922e";

  public static final String WEBSITE_URL = "https://www.ardmediathek.de/ard/player/%s";

  public static final String BASE_URL_SUBTITLES = "https://classic.ardmediathek.de";

  private ArdConstants() {}
}
