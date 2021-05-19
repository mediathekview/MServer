package de.mediathekview.mserver.crawler.zdf.parser;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

public class ZdfTopicPageHtmlDeserializer {

  private static final String LINK_SELECTOR1 = "article.b-content-teaser-item h3 a";
  private final String urlApiBase;

  public ZdfTopicPageHtmlDeserializer(final String urlApiBase) {
    this.urlApiBase = urlApiBase;
  }
  //private static final String LINK_SELECTOR2 = "article.b-cluster-teaser h3 a";

  public Set<CrawlerUrlDTO> deserialize(final Document document) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    Elements filmUrls = document.select(LINK_SELECTOR1);
    //filmUrls.addAll(document.select(LINK_SELECTOR2));
    filmUrls.forEach(
            filmUrlElement -> {
              final String href = filmUrlElement.attr(ATTRIBUTE_HREF);
              final Optional<String> url = buildFilmUrlJsonFromHtmlLink(href);
              url.ifPresent(u -> results.add(new CrawlerUrlDTO(u)));
            });

    return results;
  }

  private Optional<String> buildFilmUrlJsonFromHtmlLink(String attr) {
    return UrlUtils.getFileName(attr)
            .map(s -> String.format(ZdfConstants.URL_FILM_JSON, urlApiBase, s.split("\\.")[0]));
  }
}
