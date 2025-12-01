package de.mediathekview.mserver.crawler.srf;

public final class SrfConstants {

  private SrfConstants() {}

  public static final String BASE_URL = "https://www.srf.ch";
  /**
   * URL für Übersichtsseite der Mediathek
   */
  public static final String OVERVIEW_PAGE_URL = BASE_URL + "/play/v3/api/srf/production/shows?onlyActiveShows=false";
  /**
   * URL für Übersichtsseite einer Sendung
   * Parameter: Id
   */
  public static final String SHOW_OVERVIEW_PAGE_URL = "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaList/video/latest/byShow/%s?vector=portalplay&pageSize=20";
  /**
   * URL für Detailsinformation einer Folge
   * Parameter: Id
   */
  public static final String SHOW_DETAIL_PAGE_URL = "https://il.srgssr.ch/integrationlayer/2.0/mediaComposition/byUrn/%s";

  /**
   * URL für Webseite einer Folge
   * Parameter: Thema, Titel, Id
   */
  public static final String WEBSITE_URL = "https://www.srf.ch/play/tv/%s/video/%s?id=%s";
  public static final String WEBSITE_URL_WITH_URN = "https://www.srf.ch/play/tv/%s/video/%s?urn=%s";

  /**
   * Id der Sendung SportClip, die nicht unter Sendungen A-Z gelistet ist
   */
  public static final String ID_SHOW_SPORT_CLIP = "5327eac1-e5a1-40aa-9f71-707e48258097";

  /**
   * Sendung verpasst nach Datum
   */
  public static final String SCHEDULE_PER_DAY = "https://www.srf.ch/play/v3/api/srf/production/tv-program-guide?date=%s";

  
}
