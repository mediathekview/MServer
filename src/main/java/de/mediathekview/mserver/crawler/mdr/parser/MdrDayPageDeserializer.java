package de.mediathekview.mserver.crawler.mdr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MdrDayPageDeserializer {

  private static final String FILM_URL_SELECTOR = "div.cssBroadcastList a.linkAll";

  private final String baseUrl;

  MdrDayPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {
    Set<CrawlerUrlDTO> filmPages = new HashSet<>();

    Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (Element dayLink: dayLinks) {
      String link = dayLink.attr(ATTRIBUTE_HREF);
      filmPages.add(new CrawlerUrlDTO(baseUrl + link));
    }


    return filmPages;
  }
}
