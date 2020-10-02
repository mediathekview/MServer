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

import javax.ws.rs.ProcessingException;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.net.NoRouteToHostException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ArdTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final Logger LOG = LogManager.getLogger(ArdTaskBase.class);

  private static final Type OPTIONAL_ERROR_DTO =
      new TypeToken<Optional<ArdErrorInfoDto>>() {}.getType();

  private final transient GsonBuilder gsonBuilder;

  public ArdTaskBase(
      final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<D> aUrlToCrawlDtos) {
    super(aCrawler, aUrlToCrawlDtos, Optional.empty());
    gsonBuilder = new GsonBuilder();
    registerJsonDeserializer(OPTIONAL_ERROR_DTO, new ArdErrorDeserializer());
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected <A> Optional<A> deserializeOptional(
      final WebTarget target, final Type type, final D currentElement) {
    return this.<Optional<A>>deserializeUnsafe(target, type, currentElement)
        .orElse(Optional.empty());
  }

  private <A> Optional<A> deserializeUnsafe(
      final WebTarget target, final Type type, final D currentElement) {
    final Gson gson = gsonBuilder.create();
    try {
      final Response response = executeRequest(target);
      if (response.getStatus() == 200) {
        final String jsonOutput = response.readEntity(String.class);
        if (isSuccessResponse(jsonOutput, gson, target.getUri().toString())) {
          return Optional.of(gson.fromJson(jsonOutput, type));
        }
      } else {
        LOG.error(
            "ArdTaskBase: request of url {} failed: {}", target.getUri(), response.getStatus());
      }
    } catch (final ProcessingException processingException) {
      if (processingException.getCause() instanceof NoRouteToHostException) {
        LOG.error(
            "Can't reach the target host. Trying it again by adding the URL to the URL list.",
            processingException);
        addElementToProcess(currentElement);
        crawler.decrementAndGetErrorCount();
      } else {
        LOG.error("Something went wrong deserializing {}!", target.getUri(), processingException);
      }
    }
    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget target, final Type type, final D currentElement) {
    return this.<A>deserializeUnsafe(target, type, currentElement).orElse(null);
  }

  private boolean isSuccessResponse(
      final String jsonOutput, final Gson gson, final String targetUrl) {
    final Optional<ArdErrorInfoDto> error = gson.fromJson(jsonOutput, OPTIONAL_ERROR_DTO);
    error.ifPresent(
        ardErrorInfoDto ->
            LOG.error(
                "ArdTaskBase: request of url {}} contains error: {}}, {}",
                targetUrl,
                ardErrorInfoDto.getCode(),
                ardErrorInfoDto.getMessage()));

    return error.isEmpty();
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request =
          request.header(
              ZdfConstants.HEADER_AUTHENTIFICATION, AUTHORIZATION_BEARER + authKey.get());
    }

    return request
        .header(HEADER_ACCEPT, APPLICATION_JSON)
        .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
        .get();
  }
}
