package mServer.crawler.sender.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;

abstract class ArdTeasersDeserializer {

  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";

  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NUMBER_OF_CLIPS = "numberOfClips";

  Set<ArdFilmInfoDto> parseTeasers(JsonArray teasers) {
    Set<ArdFilmInfoDto> results = new HashSet<>();
    for (JsonElement teaserElement : teasers) {
      JsonObject teaserObject = teaserElement.getAsJsonObject();
      Optional<String> id;
      int numberOfClips = 0;

      if (JsonUtils
              .checkTreePath(teaserObject, ELEMENT_LINKS, ELEMENT_TARGET)) {
        JsonObject targetObject = teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(
                ELEMENT_TARGET).getAsJsonObject();
        id = JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
      } else {
        id = JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
      }

      if (teaserObject.has(ATTRIBUTE_NUMBER_OF_CLIPS)) {
        numberOfClips = teaserObject.get(ATTRIBUTE_NUMBER_OF_CLIPS).getAsInt();
      }

      if (id.isPresent()) {
        results.add(createFilmInfo(id.get(), numberOfClips));
      }
    }

    return results;
  }

  private ArdFilmInfoDto createFilmInfo(String id, int numberOfClips) {
    final String url = ArdConstants.ITEM_URL + id;

    return new ArdFilmInfoDto(id, url, numberOfClips);
  }
}
