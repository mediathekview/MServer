package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import de.mediathekview.mserver.crawler.wdr.parser.WdrLetterPageDeserializer;
import de.mediathekview.mserver.crawler.wdr.parser.WdrLetterPageUrlDeserializer;
import java.io.IOException;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;

public class WdrLetterPageTask implements Callable<Queue<WdrTopicUrlDto>> {

  private static final Logger LOG = LogManager.getLogger(WdrLetterPageTask.class);

  private final WdrLetterPageDeserializer deserializer = new WdrLetterPageDeserializer();
  private final AbstractCrawler crawler;

  JsoupConnection jsoupConnection;

  /** @param aCrawler The crawler which uses this task. */
  public WdrLetterPageTask(final AbstractCrawler aCrawler, final JsoupConnection jsoupConnection) {
    crawler = aCrawler;
    this.jsoupConnection = jsoupConnection;
  }

  @Override
  public Queue<WdrTopicUrlDto> call() {
    final ConcurrentLinkedQueue<WdrTopicUrlDto> results = new ConcurrentLinkedQueue<>();

    final WdrLetterPageUrlDeserializer urlDeserializer = new WdrLetterPageUrlDeserializer();

    // URLs für Seiten parsen
    final Document document;
    try {
      document = jsoupConnection.getDocumentTimeoutAfter(WdrConstants.URL_LETTER_PAGE,
          (int) TimeUnit.SECONDS.toMillis(crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
    } catch (final IOException ex) {
      LOG.fatal("WdrLetterPageTask: error loading url " + WdrConstants.URL_LETTER_PAGE, ex);
      return results;
    }

    final List<String> overviewLinks = urlDeserializer.deserialize(document);

    // Sendungen für Startseite ermitteln
    parseSubPage(results, document);

    // Sendungen für die einzelnen Seiten pro Buchstabe ermitteln
    overviewLinks.forEach(
        url -> {
          try {
            final Document subpageDocument = jsoupConnection.getDocumentTimeoutAfter(url,
                (int) TimeUnit.SECONDS
                    .toMillis(crawler.getCrawlerConfig().getSocketTimeoutInSeconds()));
            parseSubPage(results, subpageDocument);
          } catch (final IOException ex) {
            LOG.fatal("WdrLetterPageTask: error parsing url " + url, ex);
          }
        });

    return results;
  }

  private void parseSubPage(
      final ConcurrentLinkedQueue<WdrTopicUrlDto> aResults, final Document aDocument) {
    final List<WdrTopicUrlDto> topics = deserializer.deserialize(aDocument);
    aResults.addAll(topics);
  }
}
