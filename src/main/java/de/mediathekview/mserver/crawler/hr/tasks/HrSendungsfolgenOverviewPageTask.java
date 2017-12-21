package de.mediathekview.mserver.crawler.hr.tasks;

import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import de.mediathekview.mserver.base.Consts;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractDocumentTask;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class HrSendungsfolgenOverviewPageTask
    extends AbstractDocumentTask<CrawlerUrlDTO, CrawlerUrlDTO> {
  private static final long serialVersionUID = -6727831751148817578L;

  public HrSendungsfolgenOverviewPageTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected AbstractUrlTask<CrawlerUrlDTO, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new HrSendungsfolgenOverviewPageTask(crawler, aURLsToCrawl);
  }

  protected String getSendungsfoleUrlSelector() {
    return ".c-teaser__headlineLink.link";
  }

  @Override
  protected void processDocument(final CrawlerUrlDTO aUrlDTO, final Document aDocument) {
    for (final Element filmUrlElement : aDocument.select(getSendungsfoleUrlSelector())) {
      if (filmUrlElement.hasAttr(Consts.ATTRIBUTE_HREF)) {
        crawler.incrementAndGetMaxCount();
        crawler.updateProgress();
        taskResults.add(new CrawlerUrlDTO(filmUrlElement.absUrl(Consts.ATTRIBUTE_HREF)));
      }
    }

  }

}
