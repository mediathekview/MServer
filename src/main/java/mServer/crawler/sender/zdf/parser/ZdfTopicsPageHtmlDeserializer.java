package mServer.crawler.sender.zdf.parser;

import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Set;

public class ZdfTopicsPageHtmlDeserializer {

  private static final String ARTICLE_SELECTOR = "article";
  private static final String LINK_SELECTOR = "h3 a";
  private static final String TEASER_SELECTOR = "dd.teaser-info span";
  private static final String ATTRIBUTE_HREF = "href";

  public Set<CrawlerUrlDTO> deserialize(final Document document) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    Elements filmUrls = document.select(ARTICLE_SELECTOR);
    filmUrls.forEach(
            articleElement -> {
              final Element filmUrlElement = articleElement.selectFirst(LINK_SELECTOR);
              final Element teaserElement = articleElement.selectFirst(TEASER_SELECTOR);
              if (filmUrlElement != null && isRelevant(teaserElement)) {
                String url = filmUrlElement.attr(ATTRIBUTE_HREF);
                url = UrlUtils.addDomainIfMissing(url, ZdfConstants.URL_BASE);
                results.add(new CrawlerUrlDTO(url));
              }
            });

    return results;
  }

  private boolean isRelevant(Element teaserElement) {
    if (teaserElement == null) {
      return true;
    }
    return !("ARD".equalsIgnoreCase(teaserElement.text()) || "funk".equalsIgnoreCase(teaserElement.text()));
  }
}
