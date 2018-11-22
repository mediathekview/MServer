package mServer.crawler.sender.newsearch;

import java.io.IOException;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ZdfIndexPageDeserializer {

  private static final Logger LOG = LogManager.getLogger(ZdfIndexPageDeserializer.class);

  private static final String QUERY_SEARCH_BEARER = "head > script";
  private static final String QUERY_VIDEO_BEARER_INDEX_PAGE = "article > script";
  private static final String QUERY_VIDEO_BEARER_SUBPAGE = "div.b-playerbox";
  private static final String QUERY_SUPAGE_URL = "div.stage-item a";
  private static final String JSON_API_TOKEN = "apiToken";
  private static final String ATTRIBUTE_JSB = "data-zdfplayer-jsb";

  public ZDFConfigurationDTO deserialize(Document document) {
    ZDFConfigurationDTO configuration = new ZDFConfigurationDTO();

    final Optional<String> searchBearer = parseBearerIndexPage(document, QUERY_SEARCH_BEARER, "'");
    if (searchBearer.isPresent()) {
      configuration.setApiToken(ZDFClient.ZDFClientMode.SEARCH, searchBearer.get());
    }

    Optional<String> videoBearer = parseBearerIndexPage(document, QUERY_VIDEO_BEARER_INDEX_PAGE, "\"");

    if (!videoBearer.isPresent()) {
      videoBearer = parseTokenFromSubPage(document);
    }

    if (videoBearer.isPresent()) {
      configuration.setApiToken(ZDFClient.ZDFClientMode.VIDEO, videoBearer.get());
    }

    return configuration;
  }

  private Optional<String> parseTokenFromSubPage(final Document aDocument) {

    Optional<String> subPageUrl = parseSubPageUrl(aDocument);
    if (subPageUrl.isPresent()) {
      Optional<Document> subPageDocument = loadPage(subPageUrl.get());
      if (subPageDocument.isPresent()) {
        return parseBearerSubPage(subPageDocument.get(), QUERY_VIDEO_BEARER_SUBPAGE, "\"");
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseSubPageUrl(Document aDocument) {
    Element subPageElement = aDocument.selectFirst(QUERY_SUPAGE_URL);
    if (subPageElement != null) {
      return Optional.of("https://www.zdf.de/" + subPageElement.attr("href"));
    }

    return Optional.empty();
  }

  private Optional<Document> loadPage(final String aUrl) {
    try {
      final Document document = Jsoup.connect(aUrl).get();
      return Optional.of(document);
    } catch (IOException ex) {
      LOG.fatal("ZdfIndexPageTask: error loading url " + aUrl, ex);
    }

    return Optional.empty();
  }

  private Optional<String> parseBearerIndexPage(final Document aDocument, final String aQuery, final String aStringQuote) {

    Elements scriptElements = aDocument.select(aQuery);
    for (Element scriptElement : scriptElements) {
      String script = scriptElement.html();

      String value = parseBearer(script, aStringQuote);
      if (!value.isEmpty()) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  private Optional<String> parseBearerSubPage(final Document aDocument, final String aQuery, final String aStringQuote) {

    Element element = aDocument.selectFirst(aQuery);
    if (element != null && element.hasAttr(ATTRIBUTE_JSB)) {
      String script = element.attr(ATTRIBUTE_JSB);

      String value = parseBearer(script, aStringQuote);
      if (!value.isEmpty()) {
        return Optional.of(value);
      }
    }

    return Optional.empty();
  }

  private String parseBearer(final String aJson, String aStringQuote) {
    String bearer = "";

    int indexToken = aJson.indexOf(JSON_API_TOKEN);

    if (indexToken > 0) {
      int indexStart = aJson.indexOf(aStringQuote, indexToken + JSON_API_TOKEN.length() + 1) + 1;
      int indexEnd = aJson.indexOf(aStringQuote, indexStart);

      if (indexStart > 0) {
        bearer = aJson.substring(indexStart, indexEnd);
      }
    }

    return bearer;
  }
}
