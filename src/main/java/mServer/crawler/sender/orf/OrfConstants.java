package mServer.crawler.sender.orf;

public final class OrfConstants {

  public static final String URL_BASE = "https://tvthek.orf.at";

  /**
   * URL für die Sendungen eines Tages Muss am Ende noch um das Datum dd.MM.yyyy ergänzt werden
   */
  public static final String URL_DAY = URL_BASE + "/schedule/";

  /**
   * Basis-URL für Übersichtsseite nach Buchstaben Muss am Ende noch um Buchstabe bzw. 0 ergänzt
   * werden
   */
  public static final String URL_SHOW_LETTER_PAGE = URL_BASE + "/profiles/letter/";

  /**
   * URL für erste Übersichtsseite nach Buchstaben
   */
  public static final String URL_SHOW_LETTER_PAGE_A = URL_SHOW_LETTER_PAGE + "A";

  /**
   * URL für verpasste Sendungen eines Tages Muss am Ende noch um Datum ergänzt werden im Format
   * DD.MM.YYYY
   */
  public static final String URL_DATE = URL_BASE + "/schedule/";

  /**
   * URL für Übersichtsseite des Archivs
   */
  public static final String URL_ARCHIVE = URL_BASE + "/archive";

  private OrfConstants() {}
}
