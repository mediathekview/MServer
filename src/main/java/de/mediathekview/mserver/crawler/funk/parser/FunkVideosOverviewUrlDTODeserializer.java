package de.mediathekview.mserver.crawler.funk.parser;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class FunkVideosOverviewUrlDTODeserializer extends AbstractFunkOverviewUrlDTODeserializer {
  private static final String JSON_ELEMENT_INCLUDES = "includes";

  public FunkVideosOverviewUrlDTODeserializer(final AbstractCrawler aCrawler) {
    super(aCrawler, true);
  }

  @Override
  protected String getArrayElementName() {
    return JSON_ELEMENT_INCLUDES;
  }

}
