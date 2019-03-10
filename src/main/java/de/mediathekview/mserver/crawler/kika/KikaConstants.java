package de.mediathekview.mserver.crawler.kika;

public final class KikaConstants {

  public static final String BASE_URL = "https://www.kika.de";

  public static final String URL_DAY_PAGE = BASE_URL + "/sendungen/ipg/ipg102.html";

  public static final String URL_TOPICS_PAGE = BASE_URL + "/sendungen/sendungenabisz100.html";
  public static final String GATHER_URL_REGEX_PATTERN = "(?<=url':')[^']*";

  private KikaConstants() {}
}
