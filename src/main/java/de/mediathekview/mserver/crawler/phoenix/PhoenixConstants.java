package de.mediathekview.mserver.crawler.phoenix;

public final class PhoenixConstants {
  private PhoenixConstants() {}

  public static final String URL_BASE = "https://www.phoenix.de";
  
  public static final String[] URL_OVERVIEW = {
      "/response/template/sendungseite_overview_json",
      "/response/template/rubrik_overview_json",
      "/response/template/dossier_overview_json"};
  
  public static final String URL_FILM_DETAIL_JSON = "/response/id/";

  public  static final String URL_VIDEO_DETAILS = "%s/php/mediaplayer/data/beitrags_details.php?id=%s";
}
