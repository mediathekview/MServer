package de.mediathekview.mserver.crawler.mdr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MdrTopicPageDeserializer {

  private static final String FILM_URL_SELECTOR = "div.cssBroadcast div.media a";
  private static final String NEXT_PAGE_SELECTOR = "div.reload a.moreBtn";

  private static final Logger LOG = LogManager.getLogger(MdrTopicPageDeserializer.class);

  private final String baseUrl;

  public MdrTopicPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public MdrTopic deserialize(final Document aDocument) {
    MdrTopic topic = new MdrTopic();

    Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (Element dayLink : dayLinks) {
      String link = dayLink.attr(ATTRIBUTE_HREF);
      topic.addFilmUrl(new CrawlerUrlDTO(baseUrl + link));
    }

    Elements nextPageElements = aDocument.select(NEXT_PAGE_SELECTOR);
    if (nextPageElements.size() == 1) {
      String nextPageLink = nextPageElements.get(0).attr(ATTRIBUTE_HREF);
      topic.setNextPage(new CrawlerUrlDTO(baseUrl + nextPageLink));
    } else if (nextPageElements.size() > 1) {
      LOG.error("more than one next page element");
    }

    return topic;
  }
}
