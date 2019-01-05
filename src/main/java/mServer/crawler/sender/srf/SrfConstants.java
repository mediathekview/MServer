package mServer.crawler.sender.srf;

public final class SrfConstants {

  private SrfConstants() {}
  
  /**
   * URL für Übersichtsseite der Mediathek
   */
  public static final String OVERVIEW_PAGE_URL = "https://www.srf.ch/play/v2/tv/shows";
  /**
   * URL für Übersichtsseite einer Sendung
   * Parameter: Id, Monat-Jahr, Anzahl der Filme pro Seite
   */
  public static final String SHOW_OVERVIEW_PAGE_URL = "https://www.srf.ch/play/v2/tv/show/%s/latestEpisodes?numberOfEpisodes=%d&tillMonth=%s&layout=json";
  /**
   * URL für Detailsinformation einer Folge
   * Parameter: Id
   */
  public static final String SHOW_DETAIL_PAGE_URL = "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/%s.json";
  /**
   * URL für Webseite einer Folge
   * Parameter: Thema, Titel, Id
   */
  public static final String WEBSITE_URL = "https://www.srf.ch/play/tv/%s/video/%s?id=%s";

  /**
   * Id der Sendung SportClip, die nicht unter Sendungen A-Z gelistet ist
   */
  public static final String ID_SHOW_SPORT_CLIP = "5327eac1-e5a1-40aa-9f71-707e48258097";
}
