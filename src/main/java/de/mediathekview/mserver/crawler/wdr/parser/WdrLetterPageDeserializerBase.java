package de.mediathekview.mserver.crawler.wdr.parser;

import org.jsoup.nodes.Element;

public abstract class WdrLetterPageDeserializerBase {
  
  private static final String SELECTOR_URL_TYPE = "p.teasertext > strong";

  protected boolean isFileUrl(final Element aTopicElement) {
    Element typeElement = aTopicElement.select(SELECTOR_URL_TYPE).first();
    if (typeElement != null) {
      final String type = typeElement.text();
      return type.equalsIgnoreCase("video");
    }
    
    return true;
  }
}
