package de.mediathekview.mserver.crawler.funk.parser;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class FunkSendungenOverviewUrlDTODeserializer
    extends AbstractFunkOverviewUrlDTODeserializer {
  private static final String JSON_ELEMENT_DATA = "data";

  public FunkSendungenOverviewUrlDTODeserializer(final AbstractCrawler aCrawler) {
    super(aCrawler, false);
  }

  @Override
  protected String getArrayElementName() {
    return JSON_ELEMENT_DATA;
  }

}
