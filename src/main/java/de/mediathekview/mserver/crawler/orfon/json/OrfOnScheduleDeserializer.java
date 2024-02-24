package de.mediathekview.mserver.crawler.orfon.json;

import com.google.gson.*;

import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnBreadCrumsUrlDTO;
import de.mediathekview.mserver.crawler.orfon.OrfOnConstants;

import java.lang.reflect.Type;
import java.util.Optional;


public class OrfOnScheduleDeserializer implements JsonDeserializer<PagedElementListDTO<OrfOnBreadCrumsUrlDTO>> {
  private static final String TAG_FILM_NAME = "title";
  private static final String TAG_FILM_ID = "id";
  
  @Override
  public PagedElementListDTO<OrfOnBreadCrumsUrlDTO> deserialize(
      final JsonElement jsonElement, final Type typeOfT, final JsonDeserializationContext context)
      throws JsonParseException {
    PagedElementListDTO<OrfOnBreadCrumsUrlDTO> collectIds = new PagedElementListDTO<>();
    final JsonArray elements = jsonElement.getAsJsonArray();
    for (JsonElement element : elements) {
      final Optional<String> name = JsonUtils.getElementValueAsString(element, TAG_FILM_NAME);
      final Optional<String> id = JsonUtils.getElementValueAsString(element, TAG_FILM_ID);
      if (id.isPresent()) {
        final String url = OrfOnConstants.EPISODE + "/" + id.get();
        collectIds.addElement(new OrfOnBreadCrumsUrlDTO(id.get(), url));
      }
    }
    return collectIds;
  }
}
