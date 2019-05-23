package de.mediathekview.mserver.crawler.wdr.parser;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.List;

public class WdrLetterPageUrlDeserializer {

  private static final String LETTER_URL_SELECTOR = "div.entries > div.entry > a";

  public List<String> deserialize(final Document aDocument) {
    final List<String> results = new ArrayList<>();

    final Elements links = aDocument.select(LETTER_URL_SELECTOR);
    links.forEach(
        element -> {
          if (element.hasAttr(HtmlConsts.ATTRIBUTE_HREF)) {
            final String subpage = element.attr(HtmlConsts.ATTRIBUTE_HREF);
            results.add(WdrConstants.URL_BASE + subpage);
          }
        });

    return results;
  }
}
