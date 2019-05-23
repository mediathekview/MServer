package de.mediathekview.mserver.crawler.swr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class SwrTopicsDeserializer {

  private static final String TOPIC_URL_SELECTOR = "div.mAZhead div.teasertext > a";

  private final String baseUrl;

  public SwrTopicsDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {

    final Set<CrawlerUrlDTO> filmPages = new HashSet<>();

    final Elements dayLinks = aDocument.select(TOPIC_URL_SELECTOR);
    for (final Element dayLink : dayLinks) {
      final String link = baseUrl + "/" + dayLink.attr(ATTRIBUTE_HREF);
      filmPages.add(new CrawlerUrlDTO(link));
    }

    return filmPages;
  }
}
