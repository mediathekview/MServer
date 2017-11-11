package de.mediathekview.mserver.crawler.ndr.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

/**
 * A task to gather film URLs for NDR from "Sendung Verpasst?".
 * 
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public class NdrSendungVerpasstTask extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = -3492685446460529493L;
  private static final String ATTRIBUTE_HREF = "href";
  private static final String FILM_URL_SELECTOR = "#program_schedule .details h3 a";

  public NdrSendungVerpasstTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new NdrSendungVerpasstTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    for (final Element filmUrlElement : aDocument.select(FILM_URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(ATTRIBUTE_HREF)) {
        taskResults.add(new CrawlerUrlDTO(filmUrlElement.absUrl(ATTRIBUTE_HREF)));
        crawler.incrementAndGetMaxCount();
        crawler.updateProgress();
      }
    }
  }

}
