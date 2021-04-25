package de.mediathekview.mserver.crawler.basic;

import org.jetbrains.annotations.Nullable;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

import static javax.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static javax.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;

public abstract class AbstractGraphQlTask<T, R, D extends GraphQlUrlDto>
    extends AbstractJsonRestTask<T, R, D> {

  protected AbstractGraphQlTask(
      AbstractCrawler crawler, Queue<D> urlToCrawlDTOs, @Nullable String authKey) {
    super(crawler, urlToCrawlDTOs, authKey);
  }

  @Override
  protected Response createResponse(final Builder request, final D dto) {
    request.header(ACCEPT_CHARSET, StandardCharsets.UTF_8);
    return request
        .header(ACCEPT_ENCODING, ENCODING_GZIP)
        .post(Entity.entity(dto.getRequestBody(), MediaType.APPLICATION_JSON_TYPE));
  }
}
