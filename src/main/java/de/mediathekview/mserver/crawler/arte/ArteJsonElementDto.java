package de.mediathekview.mserver.crawler.arte;

import java.util.Optional;
import com.google.gson.JsonElement;

public class ArteJsonElementDto {
  private final JsonElement jsonElement;
  private final Optional<String> subcategoryName;

  public ArteJsonElementDto(final JsonElement aJsonElement,
      final Optional<String> aSubcategoryName) {
    jsonElement = aJsonElement;
    subcategoryName = aSubcategoryName;
  }

  public JsonElement getJsonElement() {
    return jsonElement;
  }

  public Optional<String> getSubcategoryName() {
    return subcategoryName;
  }

}
