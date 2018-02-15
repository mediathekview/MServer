package de.mediathekview.mserver.crawler.basic;

import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import org.glassfish.jersey.client.filter.EncodingFilter;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;

/**
 * This task is based on {@link AbstractUrlTask} which takes a {@link ConcurrentLinkedQueue} of
 * {@link D} and loads the URL with REST as {@link WebTarget}.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *
 * @param <T> The type of objects which will be created from this task.
 * @param <D> A sub type of {@link CrawlerUrlDTO} which this task will use to create the result
 *        objects.
 */
public abstract class AbstractRestTask<T, D extends CrawlerUrlDTO> extends AbstractUrlTask<T, D> {
  private static final long serialVersionUID = 2590729915326002860L;
  protected static final String HEADER_AUTHORIZATION = "Authorization";
  protected final transient Optional<String> authKey;
  private final Client client;

  public AbstractRestTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs);
    authKey = aAuthKey;

    client = ClientBuilder.newClient();
    client.register(EncodingFilter.class);
    client.register(GZipEncoder.class);
    client.register(DeflateEncoder.class);
  }


  /**
   * In this method you have to use the {@link WebTarget} to create a object of the return type
   * {@link T}. Add the results to {@link AbstractUrlTask#taskResults}.
   *
   * @param aDTO A DTO containing at least the URL of the given document.
   * @param aTarget The {@link WebTarget}.
   */
  protected abstract void processRestTarget(D aDTO, WebTarget aTarget);


  @Override
  protected void processElement(final D aDTO) {
    final WebTarget target = client.target(aDTO.getUrl());
    processRestTarget(aDTO, target);
  }

}
