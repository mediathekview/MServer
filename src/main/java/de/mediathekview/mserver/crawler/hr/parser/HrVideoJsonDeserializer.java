package de.mediathekview.mserver.crawler.hr.parser;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.base.utils.UrlUtils;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDTO;
import java.lang.reflect.Type;
import java.util.Optional;

public class HrVideoJsonDeserializer implements JsonDeserializer<Optional<ArdVideoInfoDTO>> {

  private static final String ATTRIBUTE_VIDEO_URL = "videoUrl";
  private static final String ATTRIBUTE_ADAPTIVE_URL = "adaptiveStreamingUrl";

  @Override
  public Optional<ArdVideoInfoDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    JsonObject aJsonObject = aJsonElement.getAsJsonObject();
    Optional<String> url = JsonUtils.getAttributeAsString(aJsonObject, ATTRIBUTE_VIDEO_URL);
    Optional<String> adaptiveUrl = JsonUtils.getAttributeAsString(aJsonObject, ATTRIBUTE_ADAPTIVE_URL);

    if (url.isPresent() && adaptiveUrl.isPresent()) {
      return createVideoInfo(url.get(), adaptiveUrl.get());
    }
    if (url.isPresent()) {
      return createVideoInfo(url.get());
    }

    return Optional.empty();
  }

  private Optional<ArdVideoInfoDTO> createVideoInfo(String videoUrl) {
    ArdVideoInfoDTO dto = new ArdVideoInfoDTO();
    dto.put(Resolution.NORMAL, videoUrl);

    return Optional.of(dto);
  }


  private Optional<ArdVideoInfoDTO> createVideoInfo(String videoUrl, String adaptiveUrl) {

    ArdVideoInfoDTO dto = new ArdVideoInfoDTO();

    String fileType = UrlUtils.getFileType(videoUrl).get();
    String pureVideoUrl = videoUrl.substring(0, videoUrl.lastIndexOf('_')) + "_";

    // ignore first and last part (filename + filetype)
    // walk through the array from the end because the resolutions are order ascending
    String[] adaptiveUrlParts = adaptiveUrl.split(",");
    for (int i = adaptiveUrlParts.length - 2; i > 1; i--) {

      Resolution resolution = getResolutionFromAdaptiveUrlPart(adaptiveUrlParts[i]);

      if (!dto.getVideoUrls().containsKey(resolution)) {
        dto.put(resolution, buildUrl(pureVideoUrl, adaptiveUrlParts[i], fileType));
      }
    }

    return Optional.of(dto);
  }

  private String buildUrl(String pureVideoUrl, String adaptiveUrlPart, String fileType) {
    return pureVideoUrl + adaptiveUrlPart + "." + fileType;
  }

  private Resolution getResolutionFromAdaptiveUrlPart(String adaptiveUrl) {
    // the first part is the width of the video file
    String[] urlParts = adaptiveUrl.split("x");
    int width = Integer.parseInt(urlParts[0]);

    if (width >= 1280) {
      return Resolution.HD;
    }
    if (width >= 640) {
      return Resolution.NORMAL;
    }
    if (width >= 320) {
      return Resolution.SMALL;
    }

    return Resolution.VERY_SMALL;
  }
}
