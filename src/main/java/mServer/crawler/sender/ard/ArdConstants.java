package mServer.crawler.sender.ard;

public class ArdConstants {

  public static final String API_URL = "https://api.ardmediathek.de";
  public static final String BASE_URL = "https://api.ardmediathek.de/public-gateway";

  public static final String ITEM_URL = API_URL + "/page-gateway/pages/ard/item/%s?embedded=true&mcV6=true";

  public static final String TOPICS_URL = API_URL + "/page-gateway/pages/%s/editorial/experiment-a-z?embedded=false";
  public static final String TOPICS_COMPILATION_URL = API_URL + "/page-gateway/widgets/%s/editorials/%s?pageNumber=0&pageSize=%s";
  public static final String TOPIC_URL = API_URL + "/page-gateway/widgets/ard/asset/%s?pageSize=%d";
  public static final String DAY_PAGE_URL = "https://programm-api.ard.de/program/api/program?day=%s&channelIds=%s&mode=channel";

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
          "phoenix"
      };

  public static final String[] IGNORED_SENDER = new String[] {"zdf", "kika", "3sat", "arte"};

  public static final String WEBSITE_URL = "https://www.ardmediathek.de/video/%s";

  public static final String BASE_URL_SUBTITLES = "https://classic.ardmediathek.de";

  private ArdConstants() {}
}
