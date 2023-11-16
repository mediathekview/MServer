package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;

public abstract class ArteTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {
  private static final Logger LOG = LogManager.getLogger(ArteTaskBase.class);
  private final transient GsonBuilder gsonBuilder;

  protected ArteTaskBase(
      final AbstractCrawler crawler,
      final Queue<D> urlToCrawlDtos,
      @Nullable final String authToken) {
    super(crawler, urlToCrawlDtos, authToken);
    gsonBuilder = new GsonBuilder();

    // limiter bei ersten aufruf bauen?
  }

  protected void registerJsonDeserializer(final Type type, final Object deserializer) {
    gsonBuilder.registerTypeAdapter(type, deserializer);
  }

  /**
   * Try to request a WebTarget resource Retry 5 time to get the data in case of http error 404
   * error is not a reason for retry and null is returned Mainly this is to take care of ARTE 429
   * (too many request) response
   *
   * @param target
   * @return
   */
  protected String requestWebtarget(final WebTarget target) {
    WebTarget webTarget = target;
    int retry = 0;
    int status = 0;
    while (retry < 5) {
      try(Response response = executeRequest(webTarget)) {
        status = response.getStatus();
        if (status == 200) {
          return response.readEntity(String.class);
        } else if (status == 404) {
          LOG.warn(
              "ArteTaskBase: attempt {} failed 404 for url {}",
              retry,
              webTarget.getUri());
          return null;
        }
      } catch (final Exception e) {
        String msg = status + status == 0 ? "" :  e.getMessage() ;
        if (retry == 4) {
          LOG.error(
              "ArteTaskBase: attempt {} failed {} for url {}",
              retry,
              msg,
              webTarget.getUri());
        } else {
          LOG.warn(
              "ArteTaskBase: attempt {} failed {} for url {}",
              retry,
              msg,
              webTarget.getUri());
        }
        retry++;
      }
      webTarget = createWebTarget(webTarget.getUri().toString());
    }
    return null;
  }

  protected <O> Optional<O> deserializeOptional(final WebTarget aTarget, final Type aType) {
    final String inputString = requestWebtarget(aTarget);
    if (inputString != null) {
      final Gson gson = gsonBuilder.create();
      return gson.fromJson(inputString, aType);
    }
    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget aTarget, final Type aType) {
    final String inputString = requestWebtarget(aTarget);
    if (inputString != null) {
      final Gson gson = gsonBuilder.create();
      return gson.fromJson(inputString, aType);
    }
    return null;
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    final Optional<String> authKey = getAuthKey();
    if (authKey.isPresent()) {
      request = request.header(HEADER_AUTHORIZATION, authKey.get());
    }
    return request
        .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
        .header(HEADER_ACCEPT, APPLICATION_JSON)
        .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
        .header("User-Agent", "Mozilla")
        .get();
  }
}
