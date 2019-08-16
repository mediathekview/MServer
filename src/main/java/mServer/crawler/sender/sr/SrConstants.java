package mServer.crawler.sender.sr;

public final class SrConstants {
  
  private SrConstants() {}

  public static final String URL_BASE = "https://www.sr-mediathek.de/";
  
  /**
   * URL für Übersichtsseite nach Themen
   * Am Ende muss noch eine Buchstabenkombination ergänzt werden, z.B. "abc", "ziffern"
   */
  public static final String URL_OVERVIEW_PAGE = "https://www.sr-mediathek.de/index.php?seite=5&a_z=";
  
  /**
   * URL für Archivseite einer Sendung
   * Parameter: Kürzel der Sendung, Seitennummer
   */
  public static final String URL_SHOW_ARCHIVE_PAGE = "http://www.sr-mediathek.de/index.php?seite=10&sen=%s&s=%s";
  
  /**
   * URL für die Detailseite eines Films
   * Parameter: Id
   */
  public static final String URL_FILM_DETAIL = "https://www.sr-mediathek.de/index.php?seite=7&id=%s";
}
