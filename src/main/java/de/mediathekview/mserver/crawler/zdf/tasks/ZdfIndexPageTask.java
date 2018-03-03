package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mserver.crawler.zdf.ZdfConfiguration;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ZdfIndexPageTask implements Callable<ZdfConfiguration> {

  private static final Logger LOG = LogManager.getLogger(ZdfIndexPageTask.class);

  private static final String QUERY_SEARCH_BEARER = "head > script";
  private static final String QUERY_VIDEO_BEARER = "article > script";
  private static final String JSON_API_TOKEN = "apiToken";

  @Override
  public ZdfConfiguration call() throws Exception {
    final ZdfConfiguration configuration = new ZdfConfiguration();

    final Optional<Document> document = loadIndexPage();
    if (document.isPresent()) {

      final Optional<String> searchBearer = parseBearer(document.get(), QUERY_SEARCH_BEARER, "'");
      searchBearer.ifPresent(configuration::setSearchAuthKey);

      final Optional<String> videoBearer = parseBearer(document.get(), QUERY_VIDEO_BEARER, "\"");
      videoBearer.ifPresent(configuration::setVideoAuthKey);
    }

    return configuration;
  }

  private Optional<Document> loadIndexPage() {
    try {
      final Document document = Jsoup.connect(ZdfConstants.URL_BASE).get();
      return Optional.of(document);
    } catch (IOException ex) {
      LOG.fatal("ZdfIndexPageTask: error loading url " + ZdfConstants.URL_BASE, ex);
    }

    return Optional.empty();
  }

  private Optional<String> parseBearer(final Document aDocument, final String aQuery, final String aStringQuote) {

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
