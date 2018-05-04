package mServer.crawler.sender.orf;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.Optional;

public final class JsonUtils {

  private JsonUtils() {
    super();
  }

  /**
   * Gets the value of an attribute
   *
   * @param aJsonObject the object
   * @param aAttributeName the name of the attribute
   * @return the value of the attribute, if it exists, else Optional.empty
   */
  public static Optional<String> getAttributeAsString(final JsonObject aJsonObject,
          final String aAttributeName) {
    if (aJsonObject.has(aAttributeName)) {
      final JsonElement aElement = aJsonObject.get(aAttributeName);
      if (!aElement.isJsonNull()) {
        return Optional.of(aElement.getAsString());
      }
    }

    return Optional.empty();
  }
}
