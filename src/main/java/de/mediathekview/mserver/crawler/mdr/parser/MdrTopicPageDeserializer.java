package de.mediathekview.mserver.crawler.mdr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class MdrTopicPageDeserializer {

  private static final String FILM_URL_SELECTOR =
      "div.cssBroadcast div.teaser a.linkAll,div.cssVideo div.teaser a.linkAll";
  private static final String NEXT_PAGE_SELECTOR = "div.reload a.moreBtn";

  private static final Logger LOG = LogManager.getLogger(MdrTopicPageDeserializer.class);

  private final String baseUrl;

  public MdrTopicPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public MdrTopic deserialize(final Document aDocument) {
    final MdrTopic topic = new MdrTopic();

    final Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (final Element dayLink : dayLinks) {
      final String link = dayLink.attr(ATTRIBUTE_HREF);
      topic.addFilmUrl(new CrawlerUrlDTO(baseUrl + link));
    }

    final Elements nextPageElements = aDocument.select(NEXT_PAGE_SELECTOR);
    if (nextPageElements.size() == 1) {
      final String nextPageLink = nextPageElements.get(0).attr(ATTRIBUTE_HREF);
      topic.setNextPage(new CrawlerUrlDTO(baseUrl + nextPageLink));
    } else if (nextPageElements.size() > 1) {
      LOG.error("more than one next page element");
    }

    return topic;
  }
}
