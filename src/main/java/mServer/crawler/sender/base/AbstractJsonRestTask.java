package mServer.crawler.sender.base;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import mServer.crawler.sender.MediathekReader;

import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;

/**
 * A abstract REST api task which requests the given url with the Funk Api settings.
 */
public abstract class AbstractJsonRestTask<T, R, D extends CrawlerUrlDTO>
        extends AbstractRestTask<T, D> {
  protected static final String ENCODING_GZIP = "gzip";
  private static final long serialVersionUID = -1090560363478964885L;
  protected final transient GsonBuilder gsonBuilder;

  protected AbstractJsonRestTask(
          final MediathekReader aCrawler,
          final ConcurrentLinkedQueue<D> urlToCrawlDTOs,
          final Optional<String> authKey) {
    super(aCrawler, urlToCrawlDTOs, authKey);
    gsonBuilder = new GsonBuilder();
  }

  protected abstract Object getParser(D aDTO);

  protected abstract Type getType();

  protected abstract void handleHttpError(D dto, URI url, Response response);

  protected abstract void postProcessing(R aResponseObj, D aDTO);

  @Override
  protected void processRestTarget(final D aDTO, final WebTarget aTarget) {
    gsonBuilder.registerTypeAdapter(getType(), getParser(aDTO));
    final Gson gson = gsonBuilder.create();
    Builder request = aTarget.request();
    final Optional<String> authKey = getAuthKey();
    if (authKey.isPresent()) {
      request = request.header(HEADER_AUTHORIZATION, authKey.get());
    }

    final Response response = createResponse(request, aDTO);
    traceRequest(response.getLength());
    if (response.getStatus() == 200) {
      final String jsonOutput = response.readEntity(String.class);
      final R responseObj = gson.fromJson(jsonOutput, getType());
      postProcessing(responseObj, aDTO);
    } else {
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(crawler.getSendername(), RunSender.Count.FEHLVERSUCHE);
      handleHttpError(aDTO, aTarget.getUri(), response);
    }
  }

  protected Response createResponse(final Builder request, final D dto) {
    request.header(ACCEPT_CHARSET, StandardCharsets.UTF_8);
    return request.header(ACCEPT_ENCODING, ENCODING_GZIP).get();
  }
}
