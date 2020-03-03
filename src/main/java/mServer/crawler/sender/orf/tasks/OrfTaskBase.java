package mServer.crawler.sender.orf.tasks;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractUrlTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;

public abstract class OrfTaskBase<T, D extends CrawlerUrlDTO>
        extends AbstractUrlTask<T, D> {

  private static final long serialVersionUID = -4124779055395250987L;
  private static final String LOAD_DOCUMENT_HTTPERROR
          = "Some HTTP error happened while crawl the %s page \"%s\".";

  protected static final Logger ORF_LOGGER = LogManager.getLogger("OrfLogger");

  public OrfTaskBase(final MediathekReader aCrawler,
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
    if (Config.getStop()) {
      return;
    }

    try {
      long start = System.currentTimeMillis();
      // maxBodySize(0)=unlimited
      // necessary for ORF documents which are larger than the default size
      Response response = Jsoup.connect(aUrlDTO.getUrl())
              .timeout((int) TimeUnit.SECONDS.toMillis(60))
              .maxBodySize(0).execute();

      long end = System.currentTimeMillis();

      ORF_LOGGER.trace(String.format("%s: %d - %d ms", aUrlDTO.getUrl(), response.statusCode(), end - start));

      final Document document = response.parse();
      traceRequest();
      processDocument(aUrlDTO, document);
    } catch (final HttpStatusException httpStatusError) {
      ORF_LOGGER.trace(httpStatusError);
      Log.sysLog(String.format(LOAD_DOCUMENT_HTTPERROR, crawler.getSendername(), aUrlDTO.getUrl()));

      Log.errorLog(96459855,
              crawler.getSendername() + ": crawlerDocumentLoadError: " + aUrlDTO.getUrl() + ", " + httpStatusError.getStatusCode());
    } catch (final IOException ioException) {
      Log.errorLog(96459856, ioException);
      ORF_LOGGER.trace(ioException);
    } catch (final Exception exception) {
      ORF_LOGGER.trace(exception);
    }
  }
}
