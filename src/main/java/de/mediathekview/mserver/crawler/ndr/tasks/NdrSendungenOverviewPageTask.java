package de.mediathekview.mserver.crawler.ndr.tasks;

import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.ndr.NdrCrawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * A non recursive task to gather Sendung URLs for NDR from Sendungen overview page.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Jabber:</b> nicklas2751@elaon.de<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public class NdrSendungenOverviewPageTask implements Callable<Set<CrawlerUrlDTO>> {
  private static final Logger LOG = LogManager.getLogger(NdrSendungenOverviewPageTask.class);
  private static final String SENDUNGEN_OVERVIEW_PAGE_URL =
      NdrCrawler.NDR_BASE_URL + "sendungen_a-z/index.html";
  private static final String ATTRIBUTE_HREF = "href";
  private static final String SENDUNG_URL_SELECTOR = ".container .column li a";
  private final AbstractCrawler crawler;

  /** @param aCrawler The crawler which uses this task. */
  public NdrSendungenOverviewPageTask(final AbstractCrawler aCrawler) {
    crawler = aCrawler;
  }

  @Override
  public Set<CrawlerUrlDTO> call() {
    final Set<CrawlerUrlDTO> results = new HashSet<>();

    try {
      final Document document =
          Jsoup.connect(SENDUNGEN_OVERVIEW_PAGE_URL)
              .timeout(
                  (int)
                      TimeUnit.SECONDS.toMillis(
                          crawler.getCrawlerConfig().getSocketTimeoutInSeconds()))
              .get();
      for (final Element filmUrlElement : document.select(SENDUNG_URL_SELECTOR)) {
        if (filmUrlElement.hasAttr(ATTRIBUTE_HREF)) {
          results.add(new CrawlerUrlDTO(filmUrlElement.absUrl(ATTRIBUTE_HREF)));
        }
      }
    } catch (final IOException ioException) {
      LOG.fatal(
          "Something wen't terrible wrong on gathering the NDR Sendung URLs from Sendungen overview page.",
          ioException);
    }

    return results;
  }
}
