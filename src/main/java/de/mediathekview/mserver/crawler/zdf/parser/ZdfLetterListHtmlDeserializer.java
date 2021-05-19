package de.mediathekview.mserver.crawler.zdf.parser;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class ZdfLetterListHtmlDeserializer {
  private static final String LINK_SELECTOR = "ul.letter-list li a";

  public Set<CrawlerUrlDTO> deserialize(final Document document) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    Elements filmUrls = document.select(LINK_SELECTOR);
    filmUrls.forEach(
            filmUrlElement -> {
              String url = filmUrlElement.attr(ATTRIBUTE_HREF);
              url = UrlUtils.addDomainIfMissing(url, ZdfConstants.URL_BASE);
              results.add(new CrawlerUrlDTO(url));
            });

    return results;
  }

}
