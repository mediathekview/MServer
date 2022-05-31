package mServer.crawler.sender.arte;

import com.google.gson.*;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;

public class ArteCollectionChildDeserializer implements JsonDeserializer<ArteCategoryFilmsDTO> {
  private static final String ATTRIBUTE_PROGRAM_ID = "programId";
  private static final String ELEMENT_PROGRAMS = "programs";
  private static final String ELEMENT_VIDEOS = "videos";

  public ArteCategoryFilmsDTO deserialize(
          final JsonElement aJsonElement,
          final Type aType,
          final JsonDeserializationContext aJsonDeserializationContext)
          throws JsonParseException {
    final ArteCategoryFilmsDTO result = new ArteCategoryFilmsDTO();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();

      if (JsonUtils.checkTreePath(mainObj, ELEMENT_PROGRAMS)) {
        final JsonArray programs = mainObj.get(ELEMENT_PROGRAMS).getAsJsonArray();
        programs.forEach(
                program -> {
                  final JsonObject programObject = program.getAsJsonObject();
                  if (JsonUtils.checkTreePath(programObject, ELEMENT_VIDEOS)) {
                    programObject
                            .get(ELEMENT_VIDEOS)
                            .getAsJsonArray()
                            .forEach(
                                    filmElement ->
                                            JsonUtils.getAttributeAsString(filmElement.getAsJsonObject(), ATTRIBUTE_PROGRAM_ID)
                                                    .ifPresent(result::addProgramId));
                  }
                });
      }
    }
    return result;
  }

}
