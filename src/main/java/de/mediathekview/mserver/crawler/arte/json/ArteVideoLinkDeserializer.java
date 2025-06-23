package de.mediathekview.mserver.crawler.arte.json;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mediathekview.mserver.base.utils.JsonUtils;

public class ArteVideoLinkDeserializer implements JsonDeserializer<List<ArteVideoLinkDto>> {
  
  private static final String TAG_VIDEO_STREAMS = "videoStreams";
  private static final String TAG_PROGRAM_ID = "programId";
  private static final String TAG_URL = "url";
  private static final String TAG_QUALITY = "quality";
  private static final String TAG_AUDIO_SLOT = "audioSlot";
  private static final String TAG_AUDIO_CODE = "audioCode";
  private static final String TAG_AUDIO_LABEL = "audioLabel";
  private static final String TAG_AUDIO_SHORT_LABEL = "audioShortLabel";
  private static final String TAG_WIDTH = "width";
  private static final String TAG_HEIGHT = "height";

   
  @Override
  public List<ArteVideoLinkDto> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
      throws JsonParseException {
    final List<ArteVideoLinkDto> videoUrls = new ArrayList<>();
    final JsonObject searchElement = json.getAsJsonObject();
    final JsonArray itemArray = searchElement.getAsJsonArray(TAG_VIDEO_STREAMS);
    for (JsonElement arrayElement : itemArray) {
      videoUrls.add(parseVideoElement(arrayElement));
    }
    return videoUrls;
  }
  
  protected ArteVideoLinkDto parseVideoElement(final JsonElement arrayElement) {
    return new ArteVideoLinkDto(
      JsonUtils.getElementValueAsString(arrayElement, TAG_PROGRAM_ID),
      fixMissingHttpsProtocol(JsonUtils.getElementValueAsString(arrayElement, TAG_URL)),
      JsonUtils.getElementValueAsString(arrayElement, TAG_QUALITY),
      JsonUtils.getElementValueAsString(arrayElement, TAG_AUDIO_SLOT),
      JsonUtils.getElementValueAsString(arrayElement, TAG_AUDIO_CODE),
      JsonUtils.getElementValueAsString(arrayElement, TAG_AUDIO_LABEL),
      JsonUtils.getElementValueAsString(arrayElement, TAG_AUDIO_SHORT_LABEL),
      JsonUtils.getElementValueAsString(arrayElement, TAG_WIDTH),
      JsonUtils.getElementValueAsString(arrayElement, TAG_HEIGHT)
    );
  }
  
  protected Optional<String> fixMissingHttpsProtocol(Optional<String> inputUrl) {
    if (inputUrl.isEmpty() || inputUrl.get().startsWith("https:")) {
      return inputUrl;
    }
    return Optional.of(inputUrl.get().replace("http:", "https:"));
  }
}
