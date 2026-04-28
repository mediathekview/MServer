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

public class ArdTopicGroupsDeserializer implements JsonDeserializer<Set<CrawlerUrlDTO>> {
  private static final String ELEMENT_WIDGETS = "widgets";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "self";
  private static final String ELEMENT_HREF = "href";
  private final int maxPageSize = ArdConstants.TOPICS_COMPILATION_PAGE_SIZE;
  

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
    Optional<String> totalElements = JsonUtils.getElementValueAsString(compilation, "pagination", "totalElements");
    if (totalElements.isEmpty() || totalElements.get() == null || totalElements.get().trim().length() == 0 || totalElements.get().trim().equalsIgnoreCase("0")) {
      return Optional.empty();
    }
    if (JsonUtils.hasElements(compilation, ELEMENT_LINKS)) {
      final JsonElement selfLink =
          compilation.getAsJsonObject().get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET);
      final Optional<String> url = JsonUtils.getElementValueAsString(selfLink, ELEMENT_HREF);
          
      if (url.isPresent()) {
        String x = url.get().replaceAll("pageSize=\\d+", "pageSize="+this.maxPageSize);
        return Optional.of(new CrawlerUrlDTO(x));
      }
    }

    return Optional.empty();
  }
}
