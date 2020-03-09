package de.mediathekview.mserver.crawler.zdf.parser;

import static de.mediathekview.mserver.base.HtmlConsts.ATTRIBUTE_HREF;

import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ZdfDayPageHtmlDeserializer {

  private static final String LINK_SELECTOR = "article h3 a";
  private final String urlApiBase;

  public ZdfDayPageHtmlDeserializer(final String urlApiBase) {
    this.urlApiBase = urlApiBase;
  }

  public Set<CrawlerUrlDTO> deserialize(final Document document) {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    Elements filmUrls = document.select(LINK_SELECTOR);
    filmUrls.forEach(
        filmUrlElement -> {
          final Optional<String> url =
              buildFilmUrlJsonFromHtmlLink(filmUrlElement.attr(ATTRIBUTE_HREF));
          url.ifPresent(s -> results.add(new CrawlerUrlDTO(s)));
        });

    return results;
  }

  private Optional<String> buildFilmUrlJsonFromHtmlLink(String attr) {
    return UrlUtils.getFileName(attr)
        .map(s -> String.format(ZdfConstants.URL_FILM_JSON, urlApiBase, s.split("\\.")[0]));
  }
}
