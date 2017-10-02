package de.mediathekview.mserver.crawler.basic;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import de.mediathekview.mserver.base.messages.ServerMessages;

/**
 * Recursively crawls a Website.
 */
public abstract class AbstractDocumentTask<T, D extends CrawlerUrlsDTO>
    extends AbstractUrlTask<T, D> {
  private static final long serialVersionUID = -4124779055395250981L;
  private static final Logger LOG = LogManager.getLogger(AbstractDocumentTask.class);
  private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN =
      "Something terrible happened while crawl the %s page \"%s\".";
  private static final String LOAD_DOCUMENT_HTTPERROR =
      "Some HTTP error happened while crawl the %s page \"%s\".";

  public AbstractDocumentTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }


  protected abstract void processDocument(final D aUrlDTO, final Document aDocument);


  @Override
  protected void processUrl(final D aUrlDTO) {
    try {
      final Document document = Jsoup.connect(aUrlDTO.getUrl()).get();
      processDocument(aUrlDTO, document);
    } catch (final HttpStatusException httpStatusError) {
      LOG.error(
          String.format(LOAD_DOCUMENT_HTTPERROR, crawler.getSender().getName(), aUrlDTO.getUrl()));
      crawler.printMessage(ServerMessages.CRAWLER_DOCUMENT_LOAD_ERROR,
          crawler.getSender().getName(), aUrlDTO.getUrl(), httpStatusError.getStatusCode());
      crawler.incrementAndGetErrorCount();
    } catch (final IOException ioException) {
      LOG.fatal(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN, crawler.getSender().getName(),
          aUrlDTO.getUrl()), ioException);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
    }
  }

}
