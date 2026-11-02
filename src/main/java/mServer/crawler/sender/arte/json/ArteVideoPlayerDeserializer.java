package mServer.crawler.sender.arte.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import mServer.crawler.sender.base.CrawlerUrlDTO;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.Optional;

public class ArteVideoPlayerDeserializer implements JsonDeserializer<Optional<CrawlerUrlDTO>> {
  private static final String[] TAG_STREAMS = {"data", "attributes", "streams"};
  private static final String TAG_URL = "url";

  @Override
  public Optional<CrawlerUrlDTO> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    Optional<JsonElement> streamsElement = JsonUtils.getElement(json, TAG_STREAMS);
    if (streamsElement.isEmpty() || !streamsElement.get().isJsonArray()) {
      return Optional.empty();
    }

    JsonArray streams = streamsElement.get().getAsJsonArray();
    if (streams.isEmpty()) {
      return Optional.empty();
    }

    return JsonUtils.getElementValueAsString(streams.get(0), TAG_URL)
            .filter(url -> !url.isBlank())
            .map(CrawlerUrlDTO::new);
  }
}