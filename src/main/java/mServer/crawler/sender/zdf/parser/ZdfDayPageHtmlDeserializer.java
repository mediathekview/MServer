package mServer.crawler.sender.zdf.parser;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

public class ZdfDayPageHtmlDeserializer {

  private static final String ATTRIBUTE_HREF = "href";
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
              final Optional<String> url
              = buildFilmUrlJsonFromHtmlLink(filmUrlElement.attr(ATTRIBUTE_HREF));
              url.ifPresent(s -> results.add(new CrawlerUrlDTO(s)));
            });

    return results;
  }

  private Optional<String> buildFilmUrlJsonFromHtmlLink(String attr) {
    return UrlUtils.getFileName(attr)
            .map(s -> (ZdfConstants.URL_FILM_JSON).formatted(urlApiBase, s.split("\\.")[0]));
  }
}
