package de.mediathekview.mserver.crawler.mdr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class MdrDayPageDeserializer {

  private static final String FILM_URL_SELECTOR = "div.cssBroadcastList a.linkAll";

  private final String baseUrl;

  public MdrDayPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {
    final Set<CrawlerUrlDTO> filmPages = new HashSet<>();

    final Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (final Element dayLink : dayLinks) {
      final String link = dayLink.attr(ATTRIBUTE_HREF);
      filmPages.add(new CrawlerUrlDTO(baseUrl + link));
    }

    return filmPages;
  }
}
