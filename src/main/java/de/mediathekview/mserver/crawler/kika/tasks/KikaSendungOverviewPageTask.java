package de.mediathekview.mserver.crawler.kika.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaSendungOverviewPageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = -8559179959674559691L;
  private static final String URL_SELECTOR = ".sectionC .boxStandard .teaser .linkAll";

  public KikaSendungOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new KikaSendungOverviewPageTask(crawler, aURLsToCrawl);
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    for (final Element filmUrlElement : aDocument.select(URL_SELECTOR)) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        taskResults.add(new CrawlerUrlDTO(filmUrlElement.absUrl(Consts.ATTRIBUTE_HREF)));
      }
    }
  }

}
