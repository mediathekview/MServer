package mServer.crawler.sender.arte.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import mServer.crawler.sender.base.JsonUtils;

import java.lang.reflect.Type;
import java.util.Optional;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ArteVideoPlayerDeserializer implements JsonDeserializer<List<ArteVideoLinkDto>> {
  private static final String[] TAG_STREAMS = {"data", "attributes", "streams"};
  private static final Pattern LOCALE_PATH_SEGMENT =
          Pattern.compile("/(?:de|fr|pl|it|es|en|ro)/");
  private static final String TAG_VERSIONS = "versions";
  private static final String TAG_URL = "url";
  private static final String TAG_CODE = "code";
  private static final String TAG_LABEL = "label";
  private static final String TAG_SHORT_LABEL = "shortLabel";

  @Override
  public List<ArteVideoLinkDto> deserialize(
          JsonElement json, Type typeOfT, JsonDeserializationContext context) {
    final List<ArteVideoLinkDto> videoLinks = new ArrayList<>();
    final Optional<JsonElement> streamsElement = JsonUtils.getElement(json, TAG_STREAMS);
    if (streamsElement.isEmpty() || !streamsElement.get().isJsonArray()) {
      return videoLinks;
    }

    final Optional<String> programId =
            JsonUtils.getElementValueAsString(json, "data", "attributes", "metadata", "providerId");
    for (JsonElement stream : streamsElement.get().getAsJsonArray()) {
      final Optional<String> url = JsonUtils.getElementValueAsString(stream, TAG_URL)
              .filter(value -> !value.isBlank());
      final Optional<JsonElement> versionsElement = JsonUtils.getElement(stream, TAG_VERSIONS);
      if (url.isEmpty() || versionsElement.isEmpty() || !versionsElement.get().isJsonArray()) {
        continue;
      }

      final Optional<String> quality =
              JsonUtils.getElementValueAsString(stream, "mainQuality", TAG_CODE);
      final Optional<String> audioSlot = JsonUtils.getElementValueAsString(stream, "slot");
      final JsonArray versions = versionsElement.get().getAsJsonArray();
      for (JsonElement version : versions) {
        final Optional<String> code = JsonUtils.getElementValueAsString(version, TAG_CODE)
                .filter(value -> !value.isBlank());
        if (code.isEmpty()) {
          continue;
        }

        final String localizedUrl =
                LOCALE_PATH_SEGMENT
                        .matcher(url.get())
                        .replaceAll("/" + encodePathSegment(code.get()) + "/");
        videoLinks.add(
                new ArteVideoLinkDto(
                        programId,
                        Optional.of(localizedUrl),
                        quality,
                        audioSlot,
                        code,
                        JsonUtils.getElementValueAsString(version, TAG_LABEL),
                        JsonUtils.getElementValueAsString(version, TAG_SHORT_LABEL),
                        Optional.empty(),
                        Optional.empty()));
      }
    }
    return videoLinks;
  }

  private String encodePathSegment(String value) {
    return URLEncoder.encode(value, StandardCharsets.UTF_8).replace("+", "%20");
  }
}
