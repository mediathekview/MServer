package de.mediathekview.mserver.crawler.rbb;

public final class RbbConstants {
  private RbbConstants() {}

  /**
   * Base url of the RBB mediathek.
   */
  public static final String URL_BASE = "http://mediathek.rbb-online.de/tv/";

  /**
   * The topic page for letters A-K.
   */
  public static final String URL_TOPICS_A_K = URL_BASE + "sendungen-a-z?cluster=a-k";

  /**
   * The topic page for letters L-Z.
   */
  public static final String URL_TOPICS_L_Z = URL_BASE + "sendungen-a-z?cluster=l-z";
}
