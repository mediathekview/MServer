package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.base.HtmlConsts;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ZdfIndexPageTask implements Callable<ZdfConfiguration> {

  private static final Logger LOG = LogManager.getLogger(ZdfIndexPageTask.class);

  private static final String QUERY_SEARCH_BEARER = "head > script";
  private static final String QUERY_VIDEO_BEARER_INDEX_PAGE = "article > script";
  private static final String QUERY_VIDEO_BEARER_SUBPAGE = "div.b-playerbox";
  private static final String QUERY_SUPAGE_URL = "div.stage-item a";
  private static final String JSON_API_TOKEN = "apiToken";
  private static final String ATTRIBUTE_JSB = "data-zdfplayer-jsb";
  private static final String TAG_HTML = "html";
  private static final String STRING_QUOTE = "\"";
  private final AbstractCrawler crawler;
  private final String urlBase;

  /** @param aCrawler The crawler which uses this task. */
  public ZdfIndexPageTask(final AbstractCrawler aCrawler, final String aUrlBase) {
    crawler = aCrawler;
    urlBase = aUrlBase;
  }

  JsoupConnection jsoupConnection = new JsoupConnection();

  @Override
  public ZdfConfiguration call() {
    final ZdfConfiguration configuration = new ZdfConfiguration();

    final Optional<Document> document = loadPage(urlBase);
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
        return parseBearerSubPage(subPageDocument.get());
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseSubPageUrl(final Document aDocument) {
    final Elements subPageElements = aDocument.select(QUERY_SUPAGE_URL);
    for (final Element subPageElement : subPageElements) {
      if (subPageElement.hasAttr(HtmlConsts.ATTRIBUTE_HREF)) {
        final String href = subPageElement.attr(HtmlConsts.ATTRIBUTE_HREF);
        if (href.endsWith(TAG_HTML)) {
          return Optional.of(urlBase + subPageElement.attr(HtmlConsts.ATTRIBUTE_HREF));
        }
      }
    }

    return Optional.empty();
  }

  private Optional<Document> loadPage(final String url) {
    try {
      final Document document = jsoupConnection.getDocumentTimeoutAfter(url,
          (int) TimeUnit.SECONDS.toMillis(crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
      return Optional.of(document);
    } catch (final IOException ex) {
      LOG.fatal("ZdfIndexPageTask: error loading url " + url, ex);
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

  private Optional<String> parseBearerSubPage(final Document aDocument) {

    final Element element = aDocument.selectFirst(QUERY_VIDEO_BEARER_SUBPAGE);
    if (element != null && element.hasAttr(ATTRIBUTE_JSB)) {
      final String script = element.attr(ATTRIBUTE_JSB);

      final String value = parseBearer(script, STRING_QUOTE);
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
