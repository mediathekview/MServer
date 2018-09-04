package de.mediathekview.mserver.crawler.mdr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MdrLetterPageUrlDeserializer {

  private static final String LETTER_PAGE_URL_SELECTOR = "div.multiGroupNaviAlpha li.multiGroupNaviItem a.pageItem";

  private final String baseUrl;

  public MdrLetterPageUrlDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {
    Set<CrawlerUrlDTO> letterPages = new HashSet<>();

    Elements dayLinks = aDocument.select(LETTER_PAGE_URL_SELECTOR);
    for (Element dayLink: dayLinks) {
      String link = dayLink.attr(ATTRIBUTE_HREF);
      letterPages.add(new CrawlerUrlDTO(baseUrl + link));
    }

    return letterPages;
  }
}
