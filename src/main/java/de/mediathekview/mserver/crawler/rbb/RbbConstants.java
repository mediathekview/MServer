package de.mediathekview.mserver.crawler.rbb;

public final class RbbConstants {
  private RbbConstants() {}

  /**
   * Base url of the RBB mediathek.
   */
  public static final String URL_BASE = "http://mediathek.rbb-online.de";

  /**
   * The topic page for letters A-K.
   */
  public static final String URL_TOPICS_A_K = URL_BASE + "/tv/sendungen-a-z?cluster=a-k";

  /**
   * The topic page for letters L-Z.
   */
  public static final String URL_TOPICS_L_Z = URL_BASE + "/tv/sendungen-a-z?cluster=l-z";

  /**
   * The url for the json file containing the video urls
   */
  public static final String URL_VIDEO_JSON = URL_BASE + "/play/media/%s?devicetype=pc&features=hls";
}
