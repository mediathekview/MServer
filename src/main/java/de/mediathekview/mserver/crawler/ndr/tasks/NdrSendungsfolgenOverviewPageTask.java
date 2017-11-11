package de.mediathekview.mserver.crawler.ndr.tasks;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

/**
 * A recursive task to gather film URLs for NDR from a Sendungsfolgen overview page.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class NdrSendungsfolgenOverviewPageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final Logger LOG = LogManager.getLogger(NdrSendungsfolgenOverviewPageTask.class);
  private static final long serialVersionUID = -3492685446460529493L;
  private static final String ATTRIBUTE_HREF = "href";
  private static final String FILM_URL_SELECTOR = ".module .teaser h2 a";
  private static final String URL_END = ".html";
  private static final String SUBPAGE_URL_PART = "_page-%d+";

  public NdrSendungsfolgenOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  private NdrSendungsfolgenOverviewPageTask findSubPages(final Document aDocument,
      final String aPageURl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> subPages = new ConcurrentLinkedQueue<>();
    final Elements subpageElements = aDocument.select(".pagination li a span:not(\".icon\")");
    if (subpageElements.isEmpty()) {
      final Element lastSubpageElement = subpageElements.last();
      try {
        final int lastSubpageId = Integer.parseInt(lastSubpageElement.text());

        int maxSubpageId;
        if (lastSubpageId > config.getMaximumSubpages()) {
          maxSubpageId = config.getMaximumSubpages();
        } else {
          maxSubpageId = lastSubpageId;
        }

        for (int i = 1; i <= maxSubpageId; i++) {
          subPages.add(
              new CrawlerUrlDTO(aPageURl.replace(URL_END, String.format(SUBPAGE_URL_PART, i))));
        }
      } catch (final NumberFormatException numberFormatException) {
        LOG.error(String.format("Can't parse the subpage id: \"%s\" for: \"%s\".",
            lastSubpageElement.text(), aPageURl), numberFormatException);
      }
    }
    return new NdrSendungsfolgenOverviewPageTask(crawler, subPages);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new NdrSendungsfolgenOverviewPageTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    Optional<NdrSendungsfolgenOverviewPageTask> subpageCrawler;
    if (!aUrlDTO.getUrl().contains(SUBPAGE_URL_PART) && config.getMaximumSubpages() > 0) {
      subpageCrawler = Optional.of(findSubPages(aDocument, aUrlDTO.getUrl()));
      subpageCrawler.get().fork();
    } else {
      subpageCrawler = Optional.empty();
    }

    for (final Element filmUrlElement : aDocument.select(FILM_URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(ATTRIBUTE_HREF)) {
        taskResults.add(new CrawlerUrlDTO(filmUrlElement.absUrl(ATTRIBUTE_HREF)));
        crawler.incrementAndGetMaxCount();
        crawler.updateProgress();
      }
    }

    if (subpageCrawler.isPresent()) {
      taskResults.addAll(subpageCrawler.get().join());
    }

    crawler.updateProgress();
  }
}
