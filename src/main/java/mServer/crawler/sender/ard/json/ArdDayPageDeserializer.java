package mServer.crawler.sender.ard.json;

import com.google.gson.*;

import mServer.crawler.sender.ard.ArdConstants;
import mServer.crawler.sender.ard.ArdFilmInfoDto;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ArdDayPageDeserializer implements JsonDeserializer<Set<ArdFilmInfoDto>> {

  private static final String ELEMENT_CHANNELS = "channels";
  private static final String ELEMENT_LINKS = "links";
  private static final String ELEMENT_TARGET = "target";
  private static final String ELEMENT_TIMESLOTS = "timeSlots";
  private static final String ATTRIBUTE_URL_ID = "urlId";

  @Override
  public Set<ArdFilmInfoDto> deserialize(
      final JsonElement jsonElement, final Type type, final JsonDeserializationContext context) {
    final Set<ArdFilmInfoDto> results = new HashSet<>();

    final JsonObject jsonObject = jsonElement.getAsJsonObject();
    if (jsonObject.has(ELEMENT_CHANNELS)) {
      final JsonArray channels = jsonObject.get(ELEMENT_CHANNELS).getAsJsonArray();
      results.addAll(parseChannels(channels));
    }

    return results;
  }

  private Set<ArdFilmInfoDto> parseChannels(JsonArray channels) {
    Set<ArdFilmInfoDto> entries = new HashSet<>();
    for (JsonElement channel : channels) {
      final JsonArray timeSlots = channel.getAsJsonObject().get(ELEMENT_TIMESLOTS).getAsJsonArray();
      for (JsonElement timeSlot : timeSlots) {
        for (JsonElement entry : timeSlot.getAsJsonArray()) {
          final JsonObject entryObject = entry.getAsJsonObject();
          final Optional<String> id = toId(entryObject);
          id.ifPresent(s -> entries.add(createFilmInfo(s, 1)));
        }
      }
    }
    return entries;
  }

  private ArdFilmInfoDto createFilmInfo(final String id, final int numberOfClips) {
    final String url = String.format(ArdConstants.ITEM_URL, id);
    return new ArdFilmInfoDto(id, url, numberOfClips);
  }

  private Optional<String> toId(final JsonObject teaserObject) {
    if (JsonUtils.checkTreePath(teaserObject, ELEMENT_LINKS, ELEMENT_TARGET)) {
      final JsonObject targetObject =
              teaserObject.get(ELEMENT_LINKS).getAsJsonObject().get(ELEMENT_TARGET).getAsJsonObject();
      return JsonUtils.getAttributeAsString(targetObject, ATTRIBUTE_URL_ID);
    }
    return JsonUtils.getAttributeAsString(teaserObject, ATTRIBUTE_URL_ID);
  }
}
