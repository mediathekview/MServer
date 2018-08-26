package de.mediathekview.mserver.crawler.rbb;

public final class RbbConstants {

  /**
   * Base url of the RBB mediathek.
   */
  public static final String URL_BASE = "https://mediathek.rbb-online.de";

  /**
   * maximum number of days to go back from today supported by RBB if using {@Link URL_DAY_PAGE}.
   */
  public static final int MAX_SUPPORTED_DAYS_PAST = 6;

  /**
   * The url of the videos broadcasted on a day. Parameter: number of days to go back from today (0-6)
   */
  public static final String URL_DAY_PAGE = URL_BASE + "/tv/sendungVerpasst?tag=%d&topRessort=tv";

  /**
   * The topic page for letters A-K.
   */
  public static final String URL_TOPICS_A_K = URL_BASE + "/tv/sendungen-a-z?cluster=a-k";

  /**
   * The topic page for letters L-Z.
   */
  public static final String URL_TOPICS_L_Z = URL_BASE + "/tv/sendungen-a-z?cluster=l-z";

  /**
   * The url for the json file containing the video urls.
   */
  public static final String URL_VIDEO_JSON = "%s/play/media/%s?devicetype=pc&features=hls";

  private RbbConstants() {
  }
}
