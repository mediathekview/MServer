package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class WdrLetterPageUrlDeserializer {

  private static final String LETTER_URL_SELECTOR = "div.entries > div.entry > a";
  
  public List<String> deserialize(final Document aDocument) {
    final List<String> results = new ArrayList<>();
    
    Elements links = aDocument.select(LETTER_URL_SELECTOR);
    links.forEach(element -> {
      if (element.hasAttr(Consts.ATTRIBUTE_HREF)) {
        String subpage = element.attr(Consts.ATTRIBUTE_HREF);
        results.add(WdrConstants.URL_BASE + subpage);
      }
    });
      
    return results; 
  }
}
