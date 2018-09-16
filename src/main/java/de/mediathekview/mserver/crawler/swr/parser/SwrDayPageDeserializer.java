package de.mediathekview.mserver.crawler.swr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SwrDayPageDeserializer {

  private static final String FILM_URL_SELECTOR = "div.mediSvgRow h4.headline a";

  private final String baseUrl;

  public SwrDayPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {

    Set<CrawlerUrlDTO> filmPages = new HashSet<>();

    Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (Element dayLink : dayLinks) {
      String link = baseUrl + dayLink.attr(ATTRIBUTE_HREF);
      link = link.replace(SwrConstants.URL_FILM_PAGE, SwrConstants.URL_FILM_DETAIL_REQUEST);
      filmPages.add(new CrawlerUrlDTO(link));
    }

    return filmPages;
  }
}
