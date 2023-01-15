package mServer.crawler.sender.arte;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.tool.Log;

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
  private static final String JSON_ELEMENT_VALUE = "value";

  @Override
  public ArteCategoryFilmsDTO deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
    ArteCategoryFilmsDTO dto = new ArteCategoryFilmsDTO();

    JsonElement rootElement = aJsonElement;
    if(aJsonElement.getAsJsonObject().has(JSON_ELEMENT_VALUE)) {
      rootElement = aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VALUE);
    }
    final JsonElement dataElement = rootElement.getAsJsonObject().get(JSON_ELEMENT_DATA);
    if (dataElement == null || dataElement.isJsonNull() || !dataElement.isJsonArray()) {
      Log.errorLog(12834940, "data element not found");
      return dto;
    }

    for (JsonElement jsonElement : dataElement.getAsJsonArray()) {
      String programId = jsonElement.getAsJsonObject().get(JSON_ELEMENT_PROGRAMID).getAsString();
      if (programId != null) {
        if (programId.startsWith("RC-")) {
          try {
            long collectionId = Long.parseLong(programId.replace("RC-", ""));
            dto.addCollection(String.format("RC-%06d", collectionId));
          } catch (NumberFormatException e) {
            Log.errorLog(12834939, "Invalid collection id: " + programId);
          }
        } else {
          dto.addProgramId(programId);
        }
      }
    }

    dto.setNextPage(hasNextPage(rootElement.getAsJsonObject()));

    return dto;
  }

  private static boolean hasNextPage(JsonObject aJsonObject) {

    JsonElement nextPageElement = aJsonObject.get(JSON_ELEMENT_NEXTPAGE);
    return !nextPageElement.isJsonNull();
  }
}
