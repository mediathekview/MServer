package mServer.crawler.sender.kika;

public final class KikaConstants {

  public static final String BASE_URL = "https://www.kika.de";

  public static final String URL_DAY_PAGE = BASE_URL + "/sendungen/ipg/ipg102.html";

  public static final String URL_TOPICS_PAGE = BASE_URL + "/sendungen/sendungenabisz100.html";
  public static final String URL_DGS_PAGE = BASE_URL + "/videos/alle-dgs/videos-dgs-100.html";
  public static final String URL_AUDIO_DESCRIPTION_PAGE = BASE_URL + "/videos/alle-ad/videos-ad-100.html";
  public static final String GATHER_URL_REGEX_PATTERN = "(?<=url':')[^']*";
  public static final int SOCKET_TIMEOUT = 120;

  private KikaConstants() {}
}
