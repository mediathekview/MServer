package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

abstract class ArdTeasersDeserializer {

  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";

  private static final String ATTRIBUTE_ID = "id";
  private static final String ATTRIBUTE_NUMBER_OF_CLIPS = "numberOfClips";

  private static final String ELEMENT_PUBLICATION_SERVICE = "publicationService";
  private static final String ATTRIBUTE_PARTNER = "partner";
  
  Set<ArdFilmInfoDto> parseTeasers(final JsonArray teasers) {
    return StreamSupport.stream(teasers.spliterator(), true)
        .map(JsonElement::getAsJsonObject)
        .filter(this::isRelevant)
        .map(this::toFilmInfo)
        .filter(Objects::nonNull)
        .collect(Collectors.toSet());
  }

  private ArdFilmInfoDto toFilmInfo(final JsonObject teaserObject) {
    return toId(teaserObject)
        .map(id -> createFilmInfo(id, getNumberOfClips(teaserObject)))
        .orElse(null);
  }

  private int getNumberOfClips(final JsonObject teaserObject) {
    if (teaserObject.has(ATTRIBUTE_NUMBER_OF_CLIPS)) {
      return teaserObject.get(ATTRIBUTE_NUMBER_OF_CLIPS).getAsInt();
    }
    return 0;
  }

  private Optional<String> toId(final JsonObject teaserObject) {
    if (JsonUtils.checkTreePath(teaserObject, null, ELEMENT_LINKS, ELEMENT_TARGET)) {
      final JsonObject targetObject =
          teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
      return JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_ID);
    }
    return JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_ID);
  }

  private ArdFilmInfoDto createFilmInfo(final String id, final int numberOfClips) {
    final String url = String.format(ArdConstants.ITEM_URL, id);
    return new ArdFilmInfoDto(id, url, numberOfClips);
  }
  
  private boolean isRelevant(final JsonObject teaserObject) {
    if (teaserObject.has(ELEMENT_PUBLICATION_SERVICE)) {
      final JsonObject publicationService =
              teaserObject.get(ELEMENT_PUBLICATION_SERVICE).getAsJsonObject();
      final Optional<String> attributeAsString =
              JsonUtils.getAttributeAsString(publicationService, ATTRIBUTE_PARTNER);
      if (attributeAsString.isPresent()) {
        return ArdConstants.PARTNER_TO_SENDER.get(attributeAsString.get()) != null;
      }
    }

    return true;
  }
}
