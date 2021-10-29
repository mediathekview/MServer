package de.mediathekview.mserver.crawler.arte;

/** The supported arte languages. */
public enum ArteLanguage {
  DE("DE"),
  FR("FR"),
  EN("EN"),
  ES("ES"),
  PL("PL"),
  IT("IT");

  private final String languageCode;

  ArteLanguage(final String aLanguageCode) {
    languageCode = aLanguageCode;
  }

  public String getLanguageCode() {
    return languageCode;
  }
}
