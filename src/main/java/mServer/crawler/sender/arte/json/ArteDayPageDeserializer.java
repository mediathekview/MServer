package mServer.crawler.sender.arte.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mServer.crawler.sender.arte.ArteLanguage;
import mServer.crawler.sender.arte.ArteSendungOverviewDto;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArteDayPageDeserializer extends ArteFilmListDeserializer {

  private static final String ATTRIBUTE_ZONES = "zones";
  private static final String ATTRIBUTE_CODE = "code";
  private static final String JSON_ELEMENT_DATA = "data";
  private static final String ATTRIBUTE_NAME = "name";

  private static final String ZONE_NAME_GUIDE = "listing_TV_GUIDE";

  public ArteDayPageDeserializer(final ArteLanguage language) {
    super(language);
  }

  @Override
  public ArteSendungOverviewDto deserialize(
      final JsonElement aJsonElement,
      final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) {

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

  private Optional<JsonObject> getRelevantZone(final JsonElement aJsonElement) {
    if (aJsonElement.isJsonObject()) {
      final JsonObject mainObj = aJsonElement.getAsJsonObject();

      if (mainObj.has(ATTRIBUTE_ZONES) && mainObj.get(ATTRIBUTE_ZONES).isJsonArray()) {
        final JsonArray zones = mainObj.get(ATTRIBUTE_ZONES).getAsJsonArray();
        for (final JsonElement zoneElement : zones) {
          if (isZoneRelevant(zoneElement)) {
            return Optional.of(zoneElement.getAsJsonObject());
          }
        }
      }
    }

    return Optional.empty();
  }

  private boolean isZoneRelevant(final JsonElement aZoneElement) {
    if (JsonUtils.checkTreePath(aZoneElement, ATTRIBUTE_CODE, ATTRIBUTE_NAME)) {
      final JsonObject zoneObject = aZoneElement.getAsJsonObject();
      final String zoneName =
          zoneObject.get(ATTRIBUTE_CODE).getAsJsonObject().get(ATTRIBUTE_NAME).getAsString();
      return zoneName.equalsIgnoreCase(ZONE_NAME_GUIDE);
    }
    return false;
  }
}
