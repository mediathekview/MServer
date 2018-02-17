package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WdrFilmPartDeserializer {
  private static final String PART_SELECTOR = "meta[name=dcterms.hasPart]";
  
  private static final String ATTRIBUTE_CONTENT = "content";
  
  public Set<TopicUrlDTO> deserialize(final String aTopic, final Document aDocument) {
    Set<TopicUrlDTO> results = new HashSet<>();
    
    Elements parts = aDocument.select(PART_SELECTOR);
    parts.forEach(part -> {
      if (part.hasAttr(ATTRIBUTE_CONTENT)) {
        String url = part.attr(ATTRIBUTE_CONTENT);
        results.add(new TopicUrlDTO(aTopic, url));
      }
    });
    
    return results;
  }
}
