package mServer.crawler.sender.phoenix;

public final class PhoenixConstants {
  private PhoenixConstants() {}

  public static final String URL_BASE = "https://www.phoenix.de";

  public static final String URL_OVERVIEW_JSON = "/response/template/sendungseite_overview_json";

  public static final String URL_FILM_DETAIL_JSON = "/response/id/";
  public static final String URL_FILM_DETAIL_XML = "/php/mediaplayer/data/beitrags_details.php?ak=web&ptmd=true&id=";

  public static final String URL_VIDEO_DETAILS_HOST = "https://tmd.phoenix.de";
  public static final String URL_VIDEO_DETAILS_BASE = "/tmd/2/ngplayer_2_3/vod/ptmd/phoenix/";
}
