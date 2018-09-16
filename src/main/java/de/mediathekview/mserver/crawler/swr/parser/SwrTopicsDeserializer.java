package de.mediathekview.mserver.crawler.swr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SwrTopicsDeserializer {

  private static final String TOPIC_URL_SELECTOR = "div.mAZhead div.teasertext > a";

  private final String baseUrl;

  public SwrTopicsDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {

    Set<CrawlerUrlDTO> filmPages = new HashSet<>();

    Elements dayLinks = aDocument.select(TOPIC_URL_SELECTOR);
    for (Element dayLink : dayLinks) {
      String link = baseUrl + "/" + dayLink.attr(ATTRIBUTE_HREF);
      filmPages.add(new CrawlerUrlDTO(link));
    }

    return filmPages;
  }

}
