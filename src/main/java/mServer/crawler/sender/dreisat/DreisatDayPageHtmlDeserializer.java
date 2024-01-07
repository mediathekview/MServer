package mServer.crawler.sender.dreisat;

import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.UrlUtils;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class DreisatDayPageHtmlDeserializer {

  private static final String ATTRIBUTE_HREF = "href";
  private static final String LINK_SELECTOR = "article a";
  private final String urlApiBase;

  public DreisatDayPageHtmlDeserializer(final String urlApiBase) {
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
      .map(s -> (ZdfConstants.URL_FILM_JSON).formatted(urlApiBase, s.split("\\.")[0]));
  }
}
