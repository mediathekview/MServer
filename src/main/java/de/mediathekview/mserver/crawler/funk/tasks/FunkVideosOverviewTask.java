package de.mediathekview.mserver.crawler.funk.tasks;

import java.net.URI;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.core.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractUrlTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkOverviewDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkVideosOverviewUrlDTODeserializer;

public class FunkVideosOverviewTask extends AbstractFunkOverviewTask {
  private static final Logger LOG = LogManager.getLogger(FunkVideosOverviewTask.class);
  private static final long serialVersionUID = 1879593346104113710L;

  public FunkVideosOverviewTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
  }

  @Override
  protected AbstractUrlTask<FunkOverviewDTO<FunkSendungDTO>, CrawlerUrlDTO> createNewOwnInstance(
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aURLsToCrawl) {
    return new FunkVideosOverviewTask(crawler, aURLsToCrawl, authKey);
  }

  @Override
  protected Object getParser(final CrawlerUrlDTO aDTO) {
    return new FunkVideosOverviewUrlDTODeserializer(crawler);
  }

  @Override
  protected void handleHttpError(final URI aUrl, final Response aResponse) {
    crawler.printErrorMessage();
    LOG.fatal(String.format("A HTTP error %d occured when getting REST informations from: \"%s\".",
        aResponse.getStatus(), aUrl.toString()));
  }


}
