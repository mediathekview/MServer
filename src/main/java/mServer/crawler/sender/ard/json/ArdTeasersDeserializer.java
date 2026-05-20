package mServer.crawler.sender.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

abstract class ArdTeasersDeserializer {

  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";

  private static final String ATTRIBUTE_HREF = "href";
  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NUMBER_OF_CLIPS = "numberOfClips";
  private static final String ATTRIBUTE_TYPE = "type";

  Set<ArdFilmInfoDto> parseTeasers(final JsonArray teasers) {
    return StreamSupport.stream(teasers.spliterator(), true)
        .map(JsonElement::getAsJsonObject)
        .map(this::toFilmInfo)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private ArdFilmInfoDto toFilmInfo(final JsonObject teaserObject) {
    final boolean compilation = isCompilation(teaserObject);
    if (compilation) {
      final Optional<String> url = JsonUtils.getElementValueAsString(teaserObject, ELEMENT_LINKS, ELEMENT_TARGET, ATTRIBUTE_HREF);
      final Optional<String> id = toId(teaserObject);
      return url.map(s -> new ArdFilmInfoDto(id.orElse(""), s, getNumberOfClips(teaserObject), compilation)).orElse(null);
    } else {
      return toId(teaserObject)
              .map(id -> createFilmInfo(id, getNumberOfClips(teaserObject), compilation))
              .orElse(null);
    }
  }

  private boolean isCompilation(final JsonObject teaserObject) {
    if (teaserObject.has(ATTRIBUTE_TYPE)) {
      return "compilation".equals(teaserObject.get(ATTRIBUTE_TYPE).getAsString());
    }
    return false;
  }

  private int getNumberOfClips(final JsonObject teaserObject) {
    if (teaserObject.has(ATTRIBUTE_NUMBER_OF_CLIPS)) {
      return teaserObject.get(ATTRIBUTE_NUMBER_OF_CLIPS).getAsInt();
    }
    return 0;
  }

  private Optional<String> toId(final JsonObject teaserObject) {
    if (JsonUtils.checkTreePath(teaserObject, ELEMENT_LINKS, ELEMENT_TARGET)) {
      final JsonObject targetObject =
          teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
      return JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
    }
    return JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
  }

  private ArdFilmInfoDto createFilmInfo(final String id, final int numberOfClips, final boolean isCompilation) {
    String refId = id;
    if(id.contains(":")) {
      refId = id.replace(":", "%3A");
    }
    final String url = String.format(ArdConstants.ITEM_URL, refId);
    return new ArdFilmInfoDto(id, url, numberOfClips, isCompilation);
  }
}
