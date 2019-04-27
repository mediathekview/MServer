package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.arte.ArteSendungOverviewDto;
import java.lang.reflect.Type;
import java.util.Optional;

public class ArteDayPageDeserializer extends ArteFilmListDeserializer {

  private static final String ATTRIBUTE_ZONES = "zones";
  private static final String ATTRIBUTE_CODE = "code";
  private static final String JSON_ELEMENT_DATA = "data";
  private static final String ATTRIBUTE_NAME = "name";

  private static final String ZONE_NAME_GUIDE = "listing_TV_GUIDE";

  public ArteDayPageDeserializer(ArteLanguage language) {
    super(language);
  }

  @Override
  public ArteSendungOverviewDto deserialize(
      JsonElement aJsonElement,
      Type aType,
      JsonDeserializationContext aJsonDeserializationContext) {

    final ArteSendungOverviewDto result = new ArteSendungOverviewDto();
    if (aJsonElement.isJsonObject()) {
      final Optional<JsonObject> zoneObj = getRelevantZone(aJsonElement);
      if (zoneObj.isPresent()) {
        return super.deserialize(zoneObj.get(), aType, aJsonDeserializationContext);
      }
    }
    return result;
  }

  @Override
  protected String getBaseElementName() {
    return JSON_ELEMENT_DATA;
  }

  private Optional<JsonObject> getRelevantZone(JsonElement aJsonElement) {
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();

      if (mainObj.has(ATTRIBUTE_ZONES) && mainObj.get(ATTRIBUTE_ZONES).isJsonArray()) {
        final JsonArray zones = mainObj.get(ATTRIBUTE_ZONES).getAsJsonArray();
        for (JsonElement zoneElement : zones) {
          if (isZoneRelevant(zoneElement)) {
            return Optional.of(zoneElement.getAsJsonObject());
          }
        }
      }
    }

    return Optional.empty();
  }

  private boolean isZoneRelevant(JsonElement aZoneElement) {
    if (JsonUtils.checkTreePath(aZoneElement, Optional.empty(), ATTRIBUTE_CODE, ATTRIBUTE_NAME)) {
      final JsonObject zoneObject = aZoneElement.getAsJsonObject();
      String zoneName =
          zoneObject.get(ATTRIBUTE_CODE).getAsJsonObject().get(ATTRIBUTE_NAME).getAsString();
      return zoneName.equalsIgnoreCase(ZONE_NAME_GUIDE);
    }
    return false;
  }
}
