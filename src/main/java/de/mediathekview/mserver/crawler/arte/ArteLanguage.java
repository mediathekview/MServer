package de.mediathekview.mserver.crawler.arte;

/**
 * The supported arte languages.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public enum ArteLanguage {
  DE("DE"), FR("FR"), EN("EN"), ES("ES"), PL("PL");

  private String languageCode;

  ArteLanguage(final String aLanguageCode) {
    languageCode = aLanguageCode;
  }

  public String getLanguageCode() {
    return languageCode;
  }

}
