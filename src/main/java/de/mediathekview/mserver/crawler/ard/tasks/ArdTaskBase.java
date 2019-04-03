package de.mediathekview.mserver.crawler.ard.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.ard.json.ArdErrorDeserializer;
import de.mediathekview.mserver.crawler.ard.json.ArdErrorInfoDto;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ArdTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final Logger LOG = LogManager.getLogger(ArdTaskBase.class);

  private static final Type OPTIONAL_ERROR_DTO =
      new TypeToken<Optional<ArdErrorInfoDto>>() {}.getType();

  private final GsonBuilder gsonBuilder;

  public ArdTaskBase(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<D> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos, Optional.empty());
    gsonBuilder = new GsonBuilder();
    registerJsonDeserializer(OPTIONAL_ERROR_DTO, new ArdErrorDeserializer());
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected <A> Optional<A> deserializeOptional(final WebTarget target, final Type type) {
    return this.<Optional<A>>deserializeUnsafe(target, type).orElse(Optional.empty());
  }

  private <A> Optional<A> deserializeUnsafe(final WebTarget target, final Type type) {
    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(target);
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      if (isSuccessResponse(jsonOutput, gson, target.getUri().toString())) {
        return Optional.of(gson.fromJson(jsonOutput, type));
      }
    } else {
      LOG.error(
          "ArdTaskBase: request of url "
              + target.getUri().toString()
              + " failed: "
              + response.getStatus());
    }
    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget target, final Type type) {
    return this.<A>deserializeUnsafe(target, type).orElse(null);
  }

  private boolean isSuccessResponse(
      final String jsonOutput, final Gson gson, final String targetUrl) {
    final Optional<ArdErrorInfoDto> error = gson.fromJson(jsonOutput, OPTIONAL_ERROR_DTO);
    error.ifPresent(
        ardErrorInfoDto ->
            LOG.error(
                "ArdTaskBase: request of url "
                    + targetUrl
                    + " contains error: "
                    + ardErrorInfoDto.getCode()
                    + ", "
                    + ardErrorInfoDto.getMessage()));

    return !error.isPresent();
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request =
          request.header(
              ZdfConstants.HEADER_AUTHENTIFICATION, AUTHORIZATION_BEARER + authKey.get());
    }

    return request
        // .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
        .header(HEADER_ACCEPT, APPLICATION_JSON)
        .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
        .get();
  }
}
