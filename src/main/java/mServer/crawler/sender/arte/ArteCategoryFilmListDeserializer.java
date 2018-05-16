package mServer.crawler.sender.arte;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;

/**
 * Deserialisiert Ergebnisse der Anfrage den Filmen einer Kategorie.
 * Beispiel-URL:
 * https://www.arte.tv/guide/api/api/zones/de/web/videos_subcategory_CMG/?page=1&limit=100
 */
public class ArteCategoryFilmListDeserializer implements JsonDeserializer<ArteCategoryFilmsDTO> {

  private static final String JSON_ELEMENT_DATA = "data";
  private static final String JSON_ELEMENT_NEXTPAGE = "nextPage";
  private static final String JSON_ELEMENT_PROGRAMID = "programId";

  @Override
  public ArteCategoryFilmsDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
    ArteCategoryFilmsDTO dto = new ArteCategoryFilmsDTO();

    for (JsonElement jsonElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_DATA).getAsJsonArray()) {
      String programId = jsonElement.getAsJsonObject().get(JSON_ELEMENT_PROGRAMID).getAsString();
      if (programId != null) {
        dto.addProgramId(programId);
      }
    }

    dto.setNextPage(hasNextPage(aJsonElement.getAsJsonObject()));

    return dto;
  }

  private static boolean hasNextPage(JsonObject aJsonObject) {

    JsonElement nextPageElement = aJsonObject.get(JSON_ELEMENT_NEXTPAGE);
    return !nextPageElement.isJsonNull();
  }
}
