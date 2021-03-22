package de.mediathekview.mserver.crawler.basic;

import static javax.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.lang.reflect.Type;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Queue;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.jetbrains.annotations.Nullable;

/**
 * A abstract REST api task which requests the given url with the Funk Api settings.
 *
 * @author Nicklas Wiegandt (Nicklas2751)<br>
 *     <b>Mail:</b> nicklas@wiegandt.eu<br>
 *     <b>Riot.im:</b> nicklas2751:matrix.elaon.de<br>
 */
public abstract class AbstractGraphQlTask<T, R, D extends GraphQlUrlDto>
    extends AbstractJsonRestTask<T, R, D> {

  public AbstractGraphQlTask(AbstractCrawler crawler, Queue<D> urlToCrawlDTOs,
      @Nullable String authKey) {
    super(crawler, urlToCrawlDTOs, authKey);
  }

  @Override
  protected Response createResponse(final Builder request, final D dto) {
    request.header(ACCEPT_CHARSET, StandardCharsets.UTF_8);
    return request.header(ACCEPT_ENCODING, ENCODING_GZIP).post(Entity.entity(dto.getRequestBody(), MediaType.APPLICATION_JSON_TYPE));
  }
}
