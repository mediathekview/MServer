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
 * A abstract task to process REST Api urls.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 * @param <T>
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


  protected abstract void processRestTarget(D aDTO, WebTarget aTarget);


  @Override
  protected void processUrl(final D aDTO) {
    final WebTarget target = client.target(aDTO.getUrl());
    processRestTarget(aDTO, target);
  }

}
