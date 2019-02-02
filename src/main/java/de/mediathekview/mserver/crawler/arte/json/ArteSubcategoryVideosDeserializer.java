package de.mediathekview.mserver.crawler.arte.json;

import de.mediathekview.mserver.crawler.arte.ArteLanguage;

public class ArteSubcategoryVideosDeserializer extends ArteFilmListDeserializer {

  private static final String JSON_ELEMENT_DATA = "data";

  public ArteSubcategoryVideosDeserializer(final ArteLanguage language) {
    super(language);
  }

  @Override
  protected String getBaseElementName() {
    return JSON_ELEMENT_DATA;
  }
}
