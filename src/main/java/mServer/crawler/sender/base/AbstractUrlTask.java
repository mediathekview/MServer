package mServer.crawler.sender.base;

import com.google.gson.JsonObject;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;

/**
 * This task is based on {@link RecursiveTask} and takes a
 * {@link ConcurrentLinkedQueue} of {@link D}. It splits the URLs on instances
 * of it self based on the crawler configuration and calls the
 * {@link this#processUrl(CrawlerUrlDTO)} for each.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 * <b>Mail:</b> nicklas@wiegandt.eu<br>
 * <b>Jabber:</b> nicklas2751@elaon.de<br>
 * <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to
 * create the result objects.
 */
public abstract class AbstractUrlTask<T, D extends CrawlerUrlDTO>
        extends AbstractRecursivConverterTask<T, D> {

  private static final long serialVersionUID = -4077156510484515410L;

  public AbstractUrlTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return 100;
  }

  @Deprecated
  protected void processElement(final D aDTO) {
    processElement(aDTO);
  }

  protected void traceRequest() {
    increment(RunSender.Count.ANZAHL);
  }

  protected void traceRequest(long responseLength) {
    traceRequest();
    increment(RunSender.Count.SUM_DATA_BYTE, responseLength);
    increment(RunSender.Count.SUM_TRAFFIC_BYTE, responseLength);
  }

  private void increment(final RunSender.Count count) {
    FilmeSuchen.listeSenderLaufen.inc(this.crawler.getSendername(), count);
  }

  private void increment(final RunSender.Count count, final long value) {
    FilmeSuchen.listeSenderLaufen.inc(this.crawler.getSendername(), count, value);
  }
}
