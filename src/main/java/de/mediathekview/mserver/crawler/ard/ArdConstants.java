package de.mediathekview.mserver.crawler.ard;

public class ArdConstants {

  public static final String API_URL = "https://api.ardmediathek.de";
  public static final String BASE_URL = "https://api.ardmediathek.de/public-gateway";

  public static final String ITEM_URL = API_URL + "/page-gateway/pages/ard/item/%s?embedded=true&mcV6=true";

  public static final String TOPICS_URL = API_URL + "/page-gateway/pages/%s/editorial/experiment-a-z?embedded=false";
  public static final String TOPICS_COMPILATION_URL = API_URL + "/page-gateway/widgets/%s/editorials/%s?pageNumber=0&pageSize=%s";
  public static final String TOPIC_URL = API_URL + "/page-gateway/widgets/ard/asset/%s?pageSize=%d";
  public static final String DAY_PAGE_URL = API_URL + "/page-gateway/compilations/%s/pastbroadcasts?startDateTime=%sT00:00:00.000Z&endDateTime=%sT23:59:59.000Z&pageNumber=0&pageSize=%d";

  public static final int DAY_PAGE_SIZE = 100;
  public static final int TOPICS_COMPILATION_PAGE_SIZE = 200;
  public static final int TOPIC_PAGE_SIZE = 50;

  public static final String DEFAULT_CLIENT = "ard";

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
        "tagesschau24",
        "funk",
        "phoenix",
        "arte"
      };

  public static final String WEBSITE_URL = "https://www.ardmediathek.de/video/%s";

  public static final String BASE_URL_SUBTITLES = "https://classic.ardmediathek.de";

  private ArdConstants() {}
}
