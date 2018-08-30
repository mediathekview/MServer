package de.mediathekview.mserver.crawler.mdr.parser;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MdrDayPageUrlDeserializer {

  private static final String DAY_URL_SELECTOR = "div[class*=cssBroadcastDay] > a";

  private final String baseUrl;
  private final int maxDaysPast;

  public MdrDayPageUrlDeserializer(final String aBaseUrl, final int aMaxDaysPast) {
    baseUrl = aBaseUrl;
    maxDaysPast = aMaxDaysPast;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {
    Set<CrawlerUrlDTO> dayUrls = new HashSet<>();

    Elements dayLinks = aDocument.select(DAY_URL_SELECTOR);
    for (Element dayLink: dayLinks) {
      String link = dayLink.attr(ATTRIBUTE_HREF);
      dayUrls.add(new CrawlerUrlDTO(baseUrl + link));

      if (dayUrls.size() > maxDaysPast) {
        break;
      }
    }

    return dayUrls;
  }
}
