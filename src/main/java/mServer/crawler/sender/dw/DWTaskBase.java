package mServer.crawler.sender.dw;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

@SuppressWarnings("serial")
public abstract class DWTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {
  private static final Logger LOG = LogManager.getLogger(DWTaskBase.class);

  private static final RateLimiter limiter =
      RateLimiter.create(10.0);

  private final transient GsonBuilder gsonBuilder;

  protected DWTaskBase(
          final MediathekReader aCrawler, final ConcurrentLinkedQueue<D> aUrlToCrawlDtos, final Optional<String> authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    gsonBuilder = new GsonBuilder();
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected <O> Optional<O> deserializeOptional(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(aTarget);
    if (response.getStatus() == 200) {
    	// ISsue for DW Crawler: Too many "Content-Type" header values
    	response.getHeaders().putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    	final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      LOG.error(
          "request of url {} failed: {}", aTarget.getUri(), response.getStatus());
    }

    return Optional.empty();
  }

  protected <A> A deserialize(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(aTarget);
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      LOG.error(
          "request of url {} failed: {}", aTarget.getUri(), response.getStatus());
    }

    return null;
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    final Optional<String> authKey = getAuthKey();
    if (authKey.isPresent()) {
      request =
          request.header(
              ZdfConstants.HEADER_AUTHENTIFICATION, AUTHORIZATION_BEARER + authKey.get());
    }
    limiter.acquire();
    return request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();
  }
}
