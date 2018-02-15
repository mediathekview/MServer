package de.mediathekview.mserver.crawler.funk.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

/**
 * A abstract REST api task which requests the given url with the Funk Api settings.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br>
 *         <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 *
 */
public abstract class AbstractFunkRestTask<T, R, D extends CrawlerUrlDTO>
    extends AbstractRestTask<T, D> {
  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final long serialVersionUID = -1090560363478964885L;
  protected final GsonBuilder gsonBuilder;

  public AbstractFunkRestTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs, final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
    gsonBuilder = new GsonBuilder();
  }


  protected abstract Object getParser(D aDTO);


  protected abstract Type getType();

  protected abstract void postProcessing(R aResponseObj, D aDTO);


  @Override
  protected void processRestTarget(final D aDTO, final WebTarget aTarget) {
    gsonBuilder.registerTypeAdapter(getType(), getParser(aDTO));
    final Gson gson = gsonBuilder.create();
    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request = request.header(HEADER_AUTHORIZATION, authKey.get());
    }

    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();


    final String jsonOutput = response.readEntity(String.class);
    final R responseObj = gson.fromJson(jsonOutput, getType());
    postProcessing(responseObj, aDTO);
  }

}
