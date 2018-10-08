package mServer.crawler.sender.hr;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.Optional;
import mServer.crawler.sender.ard.ArdVideoDTO;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.crawler.sender.orf.JsonUtils;
import mServer.crawler.sender.phoenix.UrlUtils;

public class HrVideoJsonDeserializer implements JsonDeserializer<Optional<ArdVideoDTO>> {

  private static final String ATTRIBUTE_VIDEO_URL = "videoUrl";
  private static final String ATTRIBUTE_ADAPTIVE_URL = "adaptiveStreamingUrl";

  @Override
  public Optional<ArdVideoDTO> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

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

  private Optional<ArdVideoDTO> createVideoInfo(String videoUrl) {
    ArdVideoDTO dto = new ArdVideoDTO();
    dto.addVideo(Qualities.NORMAL, videoUrl);

    return Optional.of(dto);
  }

  private Optional<ArdVideoDTO> createVideoInfo(String videoUrl, String adaptiveUrl) {

    ArdVideoDTO dto = new ArdVideoDTO();

    String fileType = UrlUtils.getFileType(videoUrl).get();
    String pureVideoUrl = videoUrl.substring(0, videoUrl.lastIndexOf('_')) + "_";

    // ignore first and last part (filename + filetype)
    // walk through the array from the end because the resolutions are order ascending
    String[] adaptiveUrlParts = adaptiveUrl.split(",");
    for (int i = adaptiveUrlParts.length - 2; i > 1; i--) {

      Qualities resolution = getResolutionFromAdaptiveUrlPart(adaptiveUrlParts[i]);

      if (!dto.getVideoUrls().containsKey(resolution)) {
        dto.addVideo(resolution, buildUrl(pureVideoUrl, adaptiveUrlParts[i], fileType));
      }
    }

    return Optional.of(dto);
  }

  private String buildUrl(String pureVideoUrl, String adaptiveUrlPart, String fileType) {
    return pureVideoUrl + adaptiveUrlPart + "." + fileType;
  }

  private Qualities getResolutionFromAdaptiveUrlPart(String adaptiveUrl) {
    // the first part is the width of the video file
    String[] urlParts = adaptiveUrl.split("x");
    int width = Integer.parseInt(urlParts[0]);

    if (width >= 1280) {
      return Qualities.HD;
    }
    if (width >= 640) {
      return Qualities.NORMAL;
    }
    if (width >= 320) {
      return Qualities.SMALL;
    }

    return Qualities.SMALL;
  }
}
