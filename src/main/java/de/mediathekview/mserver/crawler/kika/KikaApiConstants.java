package de.mediathekview.mserver.crawler.kika;

public final class KikaApiConstants {
  //
  public static final int PAGE_LIMIT = 400;
  //
  public static final String WEBSITE = "https://www.kika.de/";
  //
  public static final String HOST = "https://www.kika.de/api/v1/kikaplayer/kikaapp";
  //
  public static final String OVERVIEW = HOST + "/api/brands?limit=" + PAGE_LIMIT;
  //
  public static final String TOPIC = HOST + "/api/brands/%s/videos?limit=" + PAGE_LIMIT;
  //
  public static final String FILM = HOST + "/api/videos/%s/player-assets";
  //
  private KikaApiConstants() {}
  //
}
