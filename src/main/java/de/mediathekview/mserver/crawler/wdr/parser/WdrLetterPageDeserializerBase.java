package de.mediathekview.mserver.crawler.wdr.parser;

import org.jsoup.nodes.Element;

public abstract class WdrLetterPageDeserializerBase {
  
  private static final String SELECTOR_URL_TYPE1 = "p.teasertext > strong";
  private static final String SELECTOR_URL_TYPE2 = "> strong";

  protected boolean isFileUrl(final Element aTopicElement, boolean defaultReturnValue) {
    Element typeElement = aTopicElement.select(SELECTOR_URL_TYPE1).first();
    if (typeElement != null) {
      final String type = typeElement.text();
      return type.equalsIgnoreCase("video");
    }
    
    typeElement = aTopicElement.select(SELECTOR_URL_TYPE2).first();
    if (typeElement != null) {
      final String type = typeElement.text();
      return type.equalsIgnoreCase("video");
    }
    
    return defaultReturnValue;
  }
}
