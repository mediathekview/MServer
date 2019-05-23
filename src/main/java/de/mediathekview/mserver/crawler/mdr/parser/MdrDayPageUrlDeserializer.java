package de.mediathekview.mserver.crawler.mdr.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class MdrDayPageUrlDeserializer {

  private static final String DAY_URL_SELECTOR = "div[class*=cssBroadcastDay] > a";

  private final String baseUrl;
  private final int maxDaysPast;

  public MdrDayPageUrlDeserializer(final String aBaseUrl, final int aMaxDaysPast) {
    baseUrl = aBaseUrl;
    maxDaysPast = aMaxDaysPast;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {
    final Set<CrawlerUrlDTO> dayUrls = new HashSet<>();

    final Elements dayLinks = aDocument.select(DAY_URL_SELECTOR);
    for (final Element dayLink : dayLinks) {
      final String link = dayLink.attr(ATTRIBUTE_HREF);
      dayUrls.add(new CrawlerUrlDTO(baseUrl + link));

      if (dayUrls.size() > maxDaysPast) {
        break;
      }
    }

    return dayUrls;
  }
}
