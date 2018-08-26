package de.mediathekview.mserver.crawler.ard.tasks;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

public class ArdGetKanaeleTask implements Callable<Set<String>> {
  private static final String ATTRIBUTE_HREF = "href";

  private static final String KANAELE_SELEKTOR = ".modSender .entry a[href]:not([href=\"#\"])";

  private static final Logger LOG = LogManager.getLogger(ArdGetKanaeleTask.class);

  private final AbstractCrawler crawler;
  private final String baseUrl;

  public ArdGetKanaeleTask(final AbstractCrawler aCrawler, final String aBaseUrl) {
    crawler = aCrawler;
    baseUrl = aBaseUrl;
  }

  @Override
  public Set<String> call() {
    try {
      final Document document = Jsoup.connect(baseUrl).get();
      return document.select(KANAELE_SELEKTOR).stream().map(element -> element.attr(ATTRIBUTE_HREF))
          .collect(Collectors.toSet());
    } catch (final HttpStatusException httpStatusException) {
      crawler.printErrorMessage();
      LOG.fatal(
          String.format("A HTTP error %d occured when getting meta informations from: \"%s\".",
              httpStatusException.getStatusCode(), baseUrl),
          httpStatusException);
    } catch (final IOException ioException) {
      crawler.printErrorMessage();
      LOG.fatal(
          String.format("A unexpected error occured when getting meta informations from: \"%s\".",
              baseUrl),
          ioException);
    }
    return Collections.emptySet();
  }

}
