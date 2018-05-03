package mServer.crawler.sender.orf.tasks;

import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.orf.CrawlerUrlDTO;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * This is a abstract task based on {@link AbstractUrlTask} which takes a
 * {@link ConcurrentLinkedQueue} of {@link D} and loads the URL with JSOUP as
 * {@link Document}.
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to
 * create the result objects.
 */
public abstract class AbstractDocumentTask<T, D extends CrawlerUrlDTO>
        extends AbstractUrlTask<T, D> {

  private static final long serialVersionUID = -4124779055395250981L;
  private static final String LOAD_DOCUMENT_HTTPERROR
          = "Some HTTP error happened while crawl the %s page \"%s\".";

  public AbstractDocumentTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  /**
   * In this method you have to use the JSOUP {@link Document} to create a
   * object of the return type {@link T}. Add the results to
   * {@link AbstractUrlTask#taskResults}.
   *
   * @param aUrlDTO A DTO containing at least the URL of the given document.
   * @param aDocument The JSOUP {@link Document}.
   */
  protected abstract void processDocument(final D aUrlDTO, final Document aDocument);

  @Override
  protected void processElement(final D aUrlDTO) {
    try {
      // maxBodySize(0)=unlimited
      // necessary for ORF documents which are larger than the default size
      final Document document = Jsoup.connect(aUrlDTO.getUrl()).maxBodySize(0).get();
      processDocument(aUrlDTO, document);
    } catch (final HttpStatusException httpStatusError) {
      Log.sysLog(String.format(LOAD_DOCUMENT_HTTPERROR, crawler.getSendername(), aUrlDTO.getUrl()));

      Log.errorLog(96459855,
              crawler.getSendername() + ": crawlerDocumentLoadError: " + aUrlDTO.getUrl() + ", " + httpStatusError.getStatusCode());
    } catch (final IOException ioException) {
      Log.errorLog(96459856, ioException);
    }
  }
}
