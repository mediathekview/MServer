package mServer.crawler.sender.ard.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class ArdTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final Logger LOG = LogManager.getLogger(ArdTaskBase.class);

  private final GsonBuilder gsonBuilder;

  public ArdTaskBase(final MediathekReader aCrawler, ConcurrentLinkedQueue<D> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos, Optional.empty());
    gsonBuilder = new GsonBuilder();
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected <T> Optional<T> deserializeOptional(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(aTarget);
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      LOG.error("ArdTaskBase: request of url " + aTarget.getUri().toString() + " failed: " + response.getStatus());
    }

    return Optional.empty();
  }

  protected <T> T deserialize(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    Response response;
    try {
      response = executeRequest(aTarget);
    } catch (Exception e) {
      LOG.error(e);
      return null;
    }
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      LOG.error("ArdTaskBase: request of url " + aTarget.getUri().toString() + " failed: " + response.getStatus());
    }

    return null;
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();

    return request
            .header(HEADER_ACCEPT, APPLICATION_JSON)
            .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
            .get();
  }
}
