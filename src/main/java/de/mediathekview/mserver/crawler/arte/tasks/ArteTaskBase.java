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

  protected <O> Optional<O> deserializeOptional(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(aTarget);
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      LOG.error(
          "ArteTaskBase: request of url {} failed: {}", aTarget.getUri(), response.getStatus());
    }

    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response;
    try {
      response = executeRequest(aTarget);
    } catch (final Exception e) {
      LOG.error(e);
      return null;
    }
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      LOG.error(
          "ArteTaskBase: request of url {} failed: {}", aTarget.getUri(), response.getStatus());
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
