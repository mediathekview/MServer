package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.*;

public class ArdTopicsDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {
  private static final String ELEMENT_WIDGETS = "widgets";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_SELF = "self";

  private static final String ATTRIBUTE_ID = "id";
  private final String sender;

  public ArdTopicsDeserializer(String sender) {
    this.sender = sender;
  }

  @Override
  public Set<CrawlerUrlDTO> deserialize(
      JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) {
    final Set<CrawlerUrlDTO> result = new HashSet<>();

    if (JsonUtils.hasElements(jsonElement, ELEMENT_WIDGETS)) {
      final JsonArray widgets = jsonElement.getAsJsonObject().getAsJsonArray(ELEMENT_WIDGETS);
      widgets.forEach(widget -> parseWidget(widget.getAsJsonObject()).ifPresent(result::add));
    }

    return result;
  }

  private Optional<CrawlerUrlDTO> parseWidget(final JsonElement compilation) {
    if (JsonUtils.hasElements(compilation, ELEMENT_LINKS)) {
      final JsonElement selfLink =
          compilation.getAsJsonObject().get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_SELF);
      final Optional<String> id =
          JsonUtils.getAttributeAsString(selfLink.getAsJsonObject(), ATTRIBUTE_ID);
      if (id.isPresent()) {
        return Optional.of(
            new CrawlerUrlDTO(
                String.format(
                    ArdConstants.TOPICS_COMPILATION_URL,
                    sender,
                    id.get(),
                    ArdConstants.TOPICS_COMPILATION_PAGE_SIZE)));
      }
    }

    return Optional.empty();
  }
}
