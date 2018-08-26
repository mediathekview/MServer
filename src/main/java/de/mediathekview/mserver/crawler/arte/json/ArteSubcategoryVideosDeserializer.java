package de.mediathekview.mserver.crawler.arte.json;

import java.util.Optional;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArteSubcategoryVideosDeserializer extends ArteFilmListDeserializer {
  private static final String JSON_ELEMENT_DATA = "data";


  public ArteSubcategoryVideosDeserializer(final AbstractCrawler aCrawler,
      final Optional<String> aSubcategoryName) {
    super(aCrawler, aSubcategoryName);
  }


  @Override
  protected String getBaseElementName() {
    return JSON_ELEMENT_DATA;
  }

}
