package de.mediathekview.mserver.crawler.orf;

public final class OrfConstants {
  
  private OrfConstants() {}
  
  public static final String URL_BASE = "http://tvthek.orf.at";
  
  /**
   * Basis-URL für Übersichtsseite nach Buchstaben
   * Muss am Ende noch um Buchstae  bzw. 0 ergänzt werden
   */
  public static final String URL_SHOW_LETTER_PAGE = URL_BASE + "/profiles/letter/";

  /**
   * URL für erste Übersichtsseite nach Buchstaben
   */
  public static final String URL_SHOW_LETTER_PAGE_A = URL_SHOW_LETTER_PAGE + "A";
  
  /**
   * URL für verpasste Sendungen eines Tages
   * Muss am Ende noch um Datum ergänzt werden im Format DD.MM.YYYY
   */
  public static final String URL_DATE = URL_BASE + "/schedule/";
  
  /**
   * URL für Übersichtsseite des Archivs
   * Muss am Ende noch um Buchstae  bzw. 0 ergänzt werden
   */
  public static final String URL_ARCHIVE = URL_BASE + "/archive/letter/";
}
