package mServer.crawler.sender.orfon;

public final class OrfOnConstants {

  public static final String FILTER_JUGENDSCHUTZ = ".*/Jugendschutz[0-9][0-9][0-9][0-9]b[0-9][0-9][0-9][0-9]_.*";
  //
  public static final String HOST = "https://api-tvthek.orf.at/api/v4.3";
  //
  public static final String SCHEDULE = HOST + "/schedule";
  //
  public static final String AZ = HOST + "/profiles/lettergroup";
  public static final int PAGE_SIZE = 200;
  //
  public static final String HISTORY = HOST + "/history";
  //
  public static final String EPISODE = HOST + "/episode";
  //
  public static final String AUTH = "Basic b3JmX29uX3Y0MzpqRlJzYk5QRmlQU3h1d25MYllEZkNMVU41WU5aMjhtdA==";
  //
  private OrfOnConstants() {}
  //
  public static String createMaxLimmitUrl(String plainUrl) {
    return plainUrl + "?limit=" + OrfOnConstants.PAGE_SIZE;
  }
}
