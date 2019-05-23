package de.mediathekview.mserver.crawler.swr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.mdr.parser.MdrTopic;
import de.mediathekview.mserver.crawler.swr.SwrConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class SwrTopicPageDeserializer {

  private static final String FILM_URL_SELECTOR = "div.teaserColored h4.headline > a";
  private static final String NEXT_PAGE_SELECTOR = "a[title=\"eine Seite vorwärts\"]";

  private final String baseUrl;

  public SwrTopicPageDeserializer(final String aBaseUrl) {
    baseUrl = aBaseUrl;
  }

  public MdrTopic deserialize(final Document aDocument) {
    final MdrTopic topic = new MdrTopic();

    final Elements dayLinks = aDocument.select(FILM_URL_SELECTOR);
    for (final Element dayLink : dayLinks) {
      String link = baseUrl + dayLink.attr(ATTRIBUTE_HREF);
      link = link.replace(SwrConstants.URL_FILM_PAGE, SwrConstants.URL_FILM_DETAIL_REQUEST);
      topic.addFilmUrl(new CrawlerUrlDTO(link));
    }

    final Elements nextPageElements = aDocument.select(NEXT_PAGE_SELECTOR);
    if (nextPageElements.size() > 1) {
      final String nextPageLink = nextPageElements.get(0).attr(ATTRIBUTE_HREF);
      topic.setNextPage(new CrawlerUrlDTO(baseUrl + nextPageLink));
    }

    return topic;
  }
}
