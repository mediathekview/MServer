package de.mediathekview.mserver.crawler.dw;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRestTask;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.lang.reflect.Type;
import java.util.Optional;
import java.util.Queue;
@SuppressWarnings("serial")
public abstract class DWTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {
  private static final Logger LOG = LogManager.getLogger(DWTaskBase.class);

  private RateLimiter limiter = null;

  private final GsonBuilder gsonBuilder;

  public DWTaskBase(
      final AbstractCrawler aCrawler, final Queue<D> aUrlToCrawlDtos, final String authKey) {
    super(aCrawler, aUrlToCrawlDtos, authKey);
    gsonBuilder = new GsonBuilder();aCrawler.getSender();
    limiter = RateLimiter.create(
    	          new MServerConfigManager().getSenderConfig(aCrawler.getSender()).getMaximumRequestsPerSecond());
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
