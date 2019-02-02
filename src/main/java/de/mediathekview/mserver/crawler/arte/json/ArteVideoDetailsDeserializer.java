package de.mediathekview.mserver.crawler.arte.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.arte.tasks.ArteVideoDetailDTO;
import java.lang.reflect.Type;
import java.util.Optional;

public class ArteVideoDetailsDeserializer implements JsonDeserializer<Optional<ArteVideoDetailDTO>> {

  private static final String JSON_OBJECT_KEY_PLAYER = "videoJsonPlayer";
  //  private static final String JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS = "videoDurationSeconds";
  private static final String JSON_OBJECT_KEY_VSR = "VSR";
  private static final String ATTRIBUTE_URL = "url";

  @Override
  public Optional<ArteVideoDetailDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext)
      throws JsonParseException {
    ArteVideoDetailDTO arteVideoDTO = new ArteVideoDetailDTO();
    if (aJsonElement.isJsonObject()
        && aJsonElement.getAsJsonObject().has(JSON_OBJECT_KEY_PLAYER)
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).isJsonObject()
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject().has(JSON_OBJECT_KEY_VSR)
        && aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject().get(JSON_OBJECT_KEY_VSR).isJsonObject()
    ) {
      JsonObject playerObject = aJsonElement.getAsJsonObject().get(JSON_OBJECT_KEY_PLAYER).getAsJsonObject();
      JsonObject vsrJsonObject = playerObject.get(JSON_OBJECT_KEY_VSR).getAsJsonObject();
      if (vsrJsonObject.has("HTTPS_HQ_1")) {
        arteVideoDTO.put(Resolution.SMALL, getVideoUrl(vsrJsonObject, "HTTPS_HQ_1"));
      } else if (vsrJsonObject.has("HTTPS_MP4_HQ_1")) {
        arteVideoDTO.put(Resolution.SMALL, getVideoUrl(vsrJsonObject, "HTTPS_MP4_HQ_1"));
      }

      if (vsrJsonObject.has("HTTPS_EQ_1")) {
        arteVideoDTO.put(Resolution.NORMAL, getVideoUrl(vsrJsonObject, "HTTPS_EQ_1"));
      } else if (vsrJsonObject.has("HTTPS_MP4_EQ_1")) {
        arteVideoDTO.put(Resolution.NORMAL, getVideoUrl(vsrJsonObject, "HTTPS_MP4_EQ_1"));
      }

      if (vsrJsonObject.has("HTTPS_SQ_1")) {
        arteVideoDTO.put(Resolution.HD, getVideoUrl(vsrJsonObject, "HTTPS_SQ_1"));
      } else if (vsrJsonObject.has("HTTPS_MP4_SQ_1")) {
        arteVideoDTO.put(Resolution.HD, getVideoUrl(vsrJsonObject, "HTTPS_MP4_SQ_1"));
      }
/*
      if(!playerObject.get(JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS).isJsonNull())
      {
        arteVideoDTO.setDurationInSeconds(playerObject.get(JSON_ELEMENT_KEY_VIDEO_DURATION_SECONDS).getAsLong());
      }*/
      return Optional.of(arteVideoDTO);
    }

    return Optional.empty();
  }

  private static String getVideoUrl(JsonObject vsrJsonObject, String qualityTag) {
    if (vsrJsonObject.has(qualityTag)) {

      Optional<String> url = JsonUtils.getAttributeAsString(vsrJsonObject.get(qualityTag).getAsJsonObject(), ATTRIBUTE_URL);
      if (url.isPresent()) {
        return url.get();
      }
    }
    return null;
  }
}
