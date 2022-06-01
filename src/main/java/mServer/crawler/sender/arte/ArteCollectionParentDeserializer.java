package mServer.crawler.sender.arte;


import com.google.gson.*;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArteCollectionParentDeserializer implements JsonDeserializer<ArteCategoryFilmsDTO> {
  private static final String ATTRIBUTE_KIND = "kind";
  private static final String ATTRIBUTE_PROGRAM_ID = "programId";
  private static final String ELEMENT_PROGRAMS = "programs";
  private static final String ELEMENT_CHILDREN = "children";

  public ArteCategoryFilmsDTO deserialize(final JsonElement aJsonElement, final Type aType, final JsonDeserializationContext aJsonDeserializationContext) throws JsonParseException {
    final ArteCategoryFilmsDTO result = new ArteCategoryFilmsDTO();
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();

      if (JsonUtils.checkTreePath(mainObj, ELEMENT_PROGRAMS)) {
        final JsonArray programs = mainObj.get(ELEMENT_PROGRAMS).getAsJsonArray();
        programs.forEach(program -> {
          final JsonObject programObject = program.getAsJsonObject();
          if (JsonUtils.checkTreePath(programObject, ELEMENT_CHILDREN)) {
            programObject.get(ELEMENT_CHILDREN).getAsJsonArray().forEach(filmElement -> {
              final JsonObject filmObject = filmElement.getAsJsonObject();
              final Optional<String> kind = JsonUtils.getAttributeAsString(filmObject, ATTRIBUTE_KIND);
              final Optional<String> programId = JsonUtils.getAttributeAsString(filmObject, ATTRIBUTE_PROGRAM_ID);

              if (kind.isPresent() && kind.get().equalsIgnoreCase("TV_SERIES") && programId.isPresent()) {
                result.addCollection(programId.get());
              }
            });
          }
        });
      }
    }
    return result;
  }
}
