package de.mediathekview.mserver.crawler.srf;

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
  
  public static final String M3U8_BANDWIDTH = "BANDWIDTH";
  public static final String M3U8_CLOSED_CAPTIONS = "CLOSED-CAPTIONS";
  public static final String M3U8_CODECS = "CODECS";
  public static final String M3U8_PROGRAM_ID = "PROGRAM-ID";
  public static final String M3U8_RESOLUTION = "RESOLUTION";
  
  public static final String ENCODING_GZIP = "gzip";
  public static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
}
