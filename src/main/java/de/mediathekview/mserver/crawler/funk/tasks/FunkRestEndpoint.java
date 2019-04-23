package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.funk.FunkApiUrls;

import java.util.Objects;

public class FunkRestEndpoint<T> {
  private final FunkApiUrls endpointUrl;
  private final JsonDeserializer<PagedElementListDTO<T>> deserializer;

  public FunkRestEndpoint(
      final FunkApiUrls endpointUrl, final JsonDeserializer<PagedElementListDTO<T>> deserializer) {
    this.endpointUrl = endpointUrl;
    this.deserializer = deserializer;
  }

  public FunkApiUrls getEndpointUrl() {
    return endpointUrl;
  }

  public JsonDeserializer<PagedElementListDTO<T>> getDeserializer() {
    return deserializer;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FunkRestEndpoint<?> that = (FunkRestEndpoint<?>) o;
    return endpointUrl == that.endpointUrl && Objects.equals(deserializer, that.deserializer);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endpointUrl, deserializer);
  }

  @Override
  public String toString() {
    return "FunkRestEndpoint{"
        + "endpointUrl="
        + endpointUrl
        + ", deserializer="
        + deserializer
        + '}';
  }
}
