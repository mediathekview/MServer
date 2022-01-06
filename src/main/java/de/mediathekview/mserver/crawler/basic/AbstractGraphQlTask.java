package de.mediathekview.mserver.crawler.basic;

import org.jetbrains.annotations.Nullable;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.Queue;

import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_CHARSET;
import static jakarta.ws.rs.core.HttpHeaders.ACCEPT_ENCODING;

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
