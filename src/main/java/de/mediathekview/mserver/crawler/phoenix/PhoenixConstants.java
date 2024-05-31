package de.mediathekview.mserver.crawler.phoenix;

import java.util.HashMap;
import java.util.Map;

import de.mediathekview.mlib.daten.Sender;

public final class PhoenixConstants {
  private PhoenixConstants() {}

  public static final String URL_BASE = "https://www.phoenix.de";

  public static final String URL_OVERVIEW_JSON = "/response/template/sendungseite_overview_json";

  public static final String URL_FILM_DETAIL_JSON = "/response/id/";

  public  static final String URL_VIDEO_DETAILS = "%s/php/mediaplayer/data/beitrags_details.php?id=%s";
  
  public static final Map<String, Sender> PARTNER_TO_SENDER = new HashMap<>();

  static {
    PARTNER_TO_SENDER.put("Phoenix", Sender.PHOENIX);
  }
  
}
