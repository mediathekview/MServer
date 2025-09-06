package de.mediathekview.mserver.crawler.funk.tasks;

import com.google.gson.JsonDeserializer;
import de.mediathekview.mserver.crawler.funk.FunkApiUrls;

import java.lang.reflect.Type;
import java.util.Objects;

public class FunkRestEndpoint<T> {
  private final FunkApiUrls endpointUrl;
  private final JsonDeserializer<T> deserializer;
  private final Type elementType;

  public FunkRestEndpoint(
      final FunkApiUrls endpointUrl, final JsonDeserializer<T> deserializer, final Type elementType) {
    this.endpointUrl = endpointUrl;
    this.deserializer = deserializer;
    this.elementType = elementType;
  }

  public FunkApiUrls getEndpointUrl() {
    return endpointUrl;
  }

  public JsonDeserializer<T> getDeserializer() {
    return deserializer;
  }

  public Type getElementType() {
    return elementType;
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
