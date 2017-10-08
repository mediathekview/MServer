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
 * @author Nicklas Wiegandt (Nicklas2751)<br/>
 *         <b>Mail:</b> nicklas@wiegandt.eu<br/>
 *         <b>Jabber:</b> nicklas2751@elaon.de<br/>
 *         <b>Skype:</b> Nicklas2751<br/>
 *
 */
public abstract class AbstractFunkRestTask<T> extends AbstractRestTask<T> {
  private static final String ENCODING_GZIP = "gzip";
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final long serialVersionUID = -1090560363478964885L;
  private final transient Gson gson;
  protected final boolean incrementMaxCount;

  public AbstractFunkRestTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlToCrawlDTOs, final boolean aIncrementMaxCount,
      final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDTOs, aAuthKey);
    incrementMaxCount = aIncrementMaxCount;
    gson = new GsonBuilder().registerTypeAdapter(getType(), getParser()).create();
  }


  protected abstract Object getParser();


  protected abstract Type getType();

  protected abstract void postProcessing(T aResponseObj, CrawlerUrlDTO aUrlDTO);


  @Override
  protected void processRestTarget(final CrawlerUrlDTO aUrlDTO, final WebTarget aTarget) {
    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request = request.header(HEADER_AUTHORIZATION, authKey.get());
    }

    final Response response = request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();


    final String jsonOutput = response.readEntity(String.class);
    final T responseObj = gson.fromJson(jsonOutput, getType());
    taskResults.add(responseObj);
    postProcessing(responseObj, aUrlDTO);
    if (incrementMaxCount) {
      crawler.incrementAndGetMaxCount();
      crawler.updateProgress();
    }
  }

}
