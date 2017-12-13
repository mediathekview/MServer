package de.mediathekview.mserver.crawler.funk.tasks;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkOverviewDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkSendungenOverviewUrlDTODeserializer;

public class FunkSendungenOverviewTask extends AbstractFunkOverviewTask {
  private static final long serialVersionUID = 2711125435341137599L;

  public FunkSendungenOverviewTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }

  @Override
  protected AbstractUrlTask<FunkOverviewDTO<FunkSendungDTO>, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new FunkSendungenOverviewTask(crawler, aURLsToCrawl, authKey);
  }

  @Override
  protected Object getParser(final CrawlerUrlDTO aDTO) {
    return new FunkSendungenOverviewUrlDTODeserializer(crawler);
  }


}
