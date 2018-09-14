package de.mediathekview.mserver.crawler.swr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopic;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class SwrTopicPageDeserializer {

  private static final String FILM_URL_SELECTOR = "div.teaserColored h4.headline > a";
  private static final String NEXT_PAGE_SELECTOR = "a[title=\"eine Seite vorwärts\"]";

  private final String baseUrl;

  public SwrTopicPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public MdrTopic deserialize(final Document aDocument) {
    MdrTopic topic = new MdrTopic();

    Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (Element dayLink : dayLinks) {
      String link = baseUrl + dayLink.attr(ATTRIBUTE_HREF);
      link = link.replace(SwrConstants.URL_FILM_PAGE, SwrConstants.URL_FILM_DETAIL_REQUEST);
      topic.addFilmUrl(new CrawlerUrlDTO(link));
    }

    Elements nextPageElements = aDocument.select(NEXT_PAGE_SELECTOR);
    if (nextPageElements.size() > 1) {
      String nextPageLink = nextPageElements.get(0).attr(ATTRIBUTE_HREF);
      topic.setNextPage(new CrawlerUrlDTO(baseUrl + nextPageLink));
    }

    return topic;
  }
}
