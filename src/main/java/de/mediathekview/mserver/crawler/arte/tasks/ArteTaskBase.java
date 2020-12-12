package de.mediathekview.mserver.crawler.arte.tasks;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;

public abstract class ArteTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final Logger LOG = LogManager.getLogger(ArteTaskBase.class);
  private static final RateLimiter limiter =
      RateLimiter.create(
          MServerConfigManager.getInstance()
              .getSenderConfig(Sender.ARTE_DE)
              .getMaximumRequestsPerSecond());
  private final GsonBuilder gsonBuilder;

  public ArteTaskBase(
      final AbstractCrawler aCrawler,
      final Queue<D> aUrlToCrawlDtos,
      @Nullable final String authToken) {
    super(aCrawler, aUrlToCrawlDtos, authToken);
    gsonBuilder = new GsonBuilder();
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  /**
   * Try to request a WebTarget resource
   * Retry 5 time to get the data in case of http error
   * 404 error is not a reason for retry and null is returned
   * Mainly this is to take care of ARTE 429 (too many request) response
   * @param aTarget
   * @return
   */
  protected String requestWebtarget(WebTarget aTarget) {
    Response response = null;
    WebTarget webTargetClone = aTarget;
    int retry = 0;
    String responseAsString = null;
    int status = 0;
    while (retry < 5) {
      try {
        response = executeRequest(webTargetClone);
        status = response.getStatus();
        if (status == 200) {
          responseAsString = response.readEntity(String.class);
          return responseAsString;
        } else if (status == 404) {
          LOG.warn(
              "ArteTaskBase: attempt {} failed 404 for url {}", retry, webTargetClone.getUri().toString());
          return null;
        } else {
          throw new Exception("HTTP: "+status);
        }
      } catch (Exception e) {
        String msg = status + "";
        if (status == 0) {
          msg = e.getMessage();
        }
        if (retry == 4) {
          LOG.error("ArteTaskBase: attempt {} failed {} for url {}", retry, msg, webTargetClone.getUri().toString());
        } else {
          LOG.warn("ArteTaskBase: attempt {} failed {} for url {}", retry, msg, webTargetClone.getUri().toString());
        }
        retry++;       
      } finally {
        try {response.close();} catch (Exception e) {}
      }
      webTargetClone = createWebTarget(webTargetClone.getUri().toString());
    }
    return responseAsString;
  }
  protected <O> Optional<O> deserializeOptional(final WebTarget aTarget, final Type aType) {
    String inputString = requestWebtarget(aTarget);
    if (inputString != null) {
      final Gson gson = gsonBuilder.create();
      return gson.fromJson(inputString, aType);
    }
    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget aTarget, final Type aType) {
    String inputString = requestWebtarget(aTarget);
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

    limiter.acquire();
    return request
        .header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP)
        .header(HEADER_ACCEPT, APPLICATION_JSON)
        .header(HEADER_CONTENT_TYPE, APPLICATION_JSON)
        .header("User-Agent", "Mozilla")
        .get();
  }
}
