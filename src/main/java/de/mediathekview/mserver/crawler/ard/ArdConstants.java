package de.mediathekview.mserver.crawler.ard;

import java.util.HashMap;
import java.util.Map;

import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;

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
        "funk",
        "alpha",
        "tagesschau24",
        "phoenix"
      };

  public static final Map<String, Sender> PARTNER_TO_SENDER = new HashMap<>();

  static {
    PARTNER_TO_SENDER.put("rbb", Sender.RBB);
    PARTNER_TO_SENDER.put("swr", Sender.SWR);
    PARTNER_TO_SENDER.put("mdr", Sender.MDR);
    PARTNER_TO_SENDER.put("ndr", Sender.NDR);
    PARTNER_TO_SENDER.put("wdr", Sender.WDR);
    PARTNER_TO_SENDER.put("hr", Sender.HR);
    PARTNER_TO_SENDER.put("br", Sender.BR);
    PARTNER_TO_SENDER.put("radio_bremen", Sender.RBTV);
    PARTNER_TO_SENDER.put("tagesschau24", Sender.ARD);
    PARTNER_TO_SENDER.put("das_erste", Sender.ARD);
    PARTNER_TO_SENDER.put("one", Sender.ONE); // ONE
    PARTNER_TO_SENDER.put("ard-alpha", Sender.ARD_ALPHA); // ARD-alpha
    PARTNER_TO_SENDER.put("funk", Sender.FUNK); // Funk.net
    PARTNER_TO_SENDER.put("sr", Sender.SR);
    PARTNER_TO_SENDER.put("phoenix", Sender.PHOENIX);
    PARTNER_TO_SENDER.put("ard", Sender.ARD);
    //IGNORED_SENDER "zdf", "kika", "3sat", "arte"
  }
  
  public static final String WEBSITE_URL = "https://www.ardmediathek.de/video/%s";

  public static final String BASE_URL_SUBTITLES = "https://classic.ardmediathek.de";
  
  // provide the same as master until crawler release
  public static Resolution getResolutionFromWidth(final int width) {
    if (width > 1280) {
      return Resolution.HD;
    } else if (width > 640) {
      return Resolution.NORMAL;
    } else {
      return Resolution.SMALL;  
    }
  }

  private ArdConstants() {}
}
