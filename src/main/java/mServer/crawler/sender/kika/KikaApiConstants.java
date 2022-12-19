package mServer.crawler.sender.kika;

public final class KikaApiConstants {

  public static final String BASE_URL = "https://www.kika.de/api/v1/kikaplayer/kikaapp/";
  public static final String ALL_VIDEOS = BASE_URL + "api/videos?limit=400&orderBy=date&orderDirection=DESC";
  public static final String WEBSITE = "https://www.kika.de/";
  public static final String FILM = BASE_URL + "/api/videos/%s/player-assets";

  private KikaApiConstants() {}
}
