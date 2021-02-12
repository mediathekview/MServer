package mServer.crawler.sender.zdf.tasks;

import com.google.common.util.concurrent.RateLimiter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.tool.Log;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;
import mServer.crawler.sender.base.AbstractRestTask;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.zdf.ZdfConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

public abstract class ZdfTaskBase<T, D extends CrawlerUrlDTO> extends AbstractRestTask<T, D> {

  private static final Logger LOG = LogManager.getLogger(ZdfTaskBase.class);

  private static final RateLimiter LIMITER = RateLimiter.create(10);

  private final transient GsonBuilder gsonBuilder;

  public ZdfTaskBase(
          final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<D> aUrlToCrawlDtos,
          final Optional<String> aAuthKey) {
    super(aCrawler, aUrlToCrawlDtos, aAuthKey);
    gsonBuilder = new GsonBuilder();
  }

  protected void registerJsonDeserializer(final Type aType, final Object aDeserializer) {
    gsonBuilder.registerTypeAdapter(aType, aDeserializer);
  }

  protected <T> Optional<T> deserializeOptional(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(aTarget);
    traceRequest(response.getLength());
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLVERSUCHE);
      LOG.error(
              "ZdfTaskBase: request of url {} failed: {}",
              aTarget.getUri(),
              response.getStatus());
      Log.sysLog(response.getStatus() + " - " + aTarget.getUri().toString());
    }

    return Optional.empty();
  }

  protected <T> T deserialize(final WebTarget aTarget, final Type aType) {

    final Gson gson = gsonBuilder.create();
    final Response response = executeRequest(aTarget);
    traceRequest(response.getLength());
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      return gson.fromJson(jsonOutput, aType);
    } else {
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLVERSUCHE);
      LOG.error(
              "ZdfTaskBase: request of url {} failed: {}",
              aTarget.getUri(),
              response.getStatus());
      Log.sysLog(response.getStatus() + " - " + aTarget.getUri().toString());
    }

    return null;
  }

  private Response executeRequest(final WebTarget aTarget) {
    Builder request = aTarget.request();
    if (authKey.isPresent()) {
      request
              = request.header(
                      ZdfConstants.HEADER_AUTHENTIFICATION, AUTHORIZATION_BEARER + authKey.get());
    }

    LIMITER.acquire();
    return request.header(HEADER_ACCEPT_ENCODING, ENCODING_GZIP).get();
  }
}
