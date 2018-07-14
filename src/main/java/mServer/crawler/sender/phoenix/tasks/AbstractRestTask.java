package mServer.crawler.sender.phoenix.tasks;

import de.mediathekview.mlib.Config;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.orf.CrawlerUrlDTO;
import mServer.crawler.sender.orf.tasks.AbstractUrlTask;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

/**
 * This task is based on {@link AbstractUrlTask} which takes a
 * {@link ConcurrentLinkedQueue} of {@link D} and loads the URL with REST as
 * {@link WebTarget}.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 * <b>Mail:</b> nicklas@wiegandt.eu<br/>
 * <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to
 * create the result objects.
 */
public abstract class AbstractRestTask<T, D extends CrawlerUrlDTO> extends AbstractUrlTask<T, D> {

  private static final long serialVersionUID = 2590729915326002860L;
  protected static final String ENCODING_GZIP = "gzip";
  protected static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  protected static final String HEADER_AUTHORIZATION = "Authorization";
  protected static final String AUTHORIZATION_BEARER = "Bearer ";

  protected final transient Optional<String> authKey;
  private final Client client;

  public AbstractRestTask(final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs);
    authKey = aAuthKey;

    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }

  /**
   * In this method you have to use the {@link WebTarget} to create a object of
   * the return type {@link T}. Add the results to
   * {@link AbstractUrlTask#taskResults}.
   *
   * @param aDTO A DTO containing at least the URL of the given document.
   * @param aTarget The {@link WebTarget}.
   */
  protected abstract void processRestTarget(D aDTO, WebTarget aTarget);

  @Override
  protected void processElement(final D aDTO) {
    if (Config.getStop()) {
      return;
    }

    final WebTarget target = createWebTarget(aDTO.getUrl());
    processRestTarget(aDTO, target);
  }

  /**
   * Creates a {@link WebTarget}.
   *
   * @param aUrl the url.
   * @return the {@link WebTarget} to access the url.
   */
  protected WebTarget createWebTarget(final String aUrl) {
    return client.target(aUrl);
  }
}
