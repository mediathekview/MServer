package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static de.mediathekview.mserver.base.Consts.ATTRIBUTE_HREF;

public class ZdfIndexPageTask implements Callable<ZdfConfiguration> {

  private static final Logger LOG = LogManager.getLogger(ZdfIndexPageTask.class);

  private static final String QUERY_SEARCH_BEARER = "head > script";
  private static final String QUERY_VIDEO_BEARER_INDEX_PAGE = "article > script";
  private static final String QUERY_VIDEO_BEARER_SUBPAGE = "div.b-playerbox";
  private static final String QUERY_SUPAGE_URL = "div.stage-item a";
  private static final String JSON_API_TOKEN = "apiToken";
  private static final String ATTRIBUTE_JSB = "data-zdfplayer-jsb";
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public ZdfIndexPageTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public ZdfConfiguration call() throws Exception {
    final ZdfConfiguration configuration = new ZdfConfiguration();

    final Optional<Document> document = loadPage(ZdfConstants.URL_BASE);
    if (document.isPresent()) {

      final Optional<String> searchBearer =
          parseBearerIndexPage(document.get(), QUERY_SEARCH_BEARER, "'");
      searchBearer.ifPresent(configuration::setSearchAuthKey);

      Optional<String> videoBearer =
          parseBearerIndexPage(document.get(), QUERY_VIDEO_BEARER_INDEX_PAGE, "\"");

      if (!videoBearer.isPresent()) {
        videoBearer = parseTokenFromSubPage(document.get());
      }

      videoBearer.ifPresent(configuration::setVideoAuthKey);
    }

    return configuration;
  }

  private Optional<String> parseTokenFromSubPage(final Document aDocument) {

    final Optional<String> subPageUrl = parseSubPageUrl(aDocument);
    if (subPageUrl.isPresent()) {
      final Optional<Document> subPageDocument = loadPage(subPageUrl.get());
      if (subPageDocument.isPresent()) {
        return parseBearerSubPage(subPageDocument.get(), QUERY_VIDEO_BEARER_SUBPAGE, "\"");
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseSubPageUrl(final Document aDocument) {
    final Element subPageElement = aDocument.selectFirst(QUERY_SUPAGE_URL);
    if (subPageElement != null) {
      return Optional.of(ZdfConstants.URL_BASE + subPageElement.attr(ATTRIBUTE_HREF));
    }

    return Optional.empty();
  }

  private Optional<Document> loadPage(final String aUrl) {
    try {
      final Document document =
          Jsoup.connect(aUrl)
              .timeout(
                  (int)
                      TimeUnit.SECONDS.toMillis(
                          crawler.getCrawlerConfig().getSocketTimeoutInSeconds()))
              .get();
      return Optional.of(document);
    } catch (final IOException ex) {
      LOG.fatal("ZdfIndexPageTask: error loading url " + aUrl, ex);
    }

    return Optional.empty();
  }

  private Optional<String> parseBearerIndexPage(
      final Document aDocument, final String aQuery, final String aStringQuote) {

    final Elements scriptElements = aDocument.select(aQuery);
    for (final Element scriptElement : scriptElements) {
      final String script = scriptElement.html();

      final String value = parseBearer(script, aStringQuote);
      if (!value.isEmpty()) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseBearerSubPage(
      final Document aDocument, final String aQuery, final String aStringQuote) {

    final Element element = aDocument.selectFirst(aQuery);
    if (element != null && element.hasAttr(ATTRIBUTE_JSB)) {
      final String script = element.attr(ATTRIBUTE_JSB);

      final String value = parseBearer(script, aStringQuote);
      if (!value.isEmpty()) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  private String parseBearer(final String aJson, final String aStringQuote) {
    String bearer = "";

    final int indexToken = aJson.indexOf(JSON_API_TOKEN);

    if (indexToken > 0) {
      final int indexStart =
          aJson.indexOf(aStringQuote, indexToken + JSON_API_TOKEN.length() + 1) + 1;
      final int indexEnd = aJson.indexOf(aStringQuote, indexStart);

      if (indexStart > 0) {
        bearer = aJson.substring(indexStart, indexEnd);
      }
    }

    return bearer;
  }
}
