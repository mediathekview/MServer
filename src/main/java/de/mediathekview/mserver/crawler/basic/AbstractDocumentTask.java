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
 * This is a abstract task based on {@link AbstractUrlTask} which takes a
 * {@link ConcurrentLinkedQueue} of {@link D} and loads the URL with JSOUP as {@link Document}.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to create the result
 *        objects.
 */
public abstract class AbstractDocumentTask<T, D extends CrawlerUrlDTO>
    extends AbstractUrlTask<T, D> {
  private static final long serialVersionUID = -4124779055395250981L;
  private static final Logger LOG = LogManager.getLogger(AbstractDocumentTask.class);
  private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN =
      "Something terrible happened while crawl the %s page \"%s\".";
  private static final String LOAD_DOCUMENT_HTTPERROR =
      "Some HTTP error happened while crawl the %s page \"%s\".";
  private boolean incrementErrorCounterOnHttpErrors;

  public AbstractDocumentTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    incrementErrorCounterOnHttpErrors = true;
  }


  /**
   * In this method you have to use the JSOUP {@link Document} to create a object of the return type
   * {@link T}. Add the results to {@link AbstractUrlTask#taskResults}.
   *
   * @param aUrlDTO A DTO containing at least the URL of the given document.
   * @param aDocument The JSOUP {@link Document}.
   */
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
      if (incrementErrorCounterOnHttpErrors) {
        crawler.incrementAndGetErrorCount();
      }
      crawler.printErrorMessage();
    }
  }

  protected void setIncrementErrorCounterOnHttpErrors(
      final boolean aIncrementErrorCounterOnHttpErrors) {
    incrementErrorCounterOnHttpErrors = aIncrementErrorCounterOnHttpErrors;
  }

}
