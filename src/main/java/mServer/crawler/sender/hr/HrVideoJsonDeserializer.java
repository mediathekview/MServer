package mServer.crawler.sender.hr;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import mServer.crawler.sender.base.Qualities;
import mServer.crawler.sender.orf.JsonUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HrVideoJsonDeserializer implements JsonDeserializer<Optional<HrVideoInfoDto>> {

  private static final String ATTRIBUTE_STREAM_URL = "streamUrl";
  private static final String ATTRIBUTE_SUBTITLE = "subtitle";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_VERTICAL_RESOLUTION = "verticalResolution";
  private static final String ELEMENT_VIDEO_RESOLUTION_LEVELS = "videoResolutionLevels";

  private static final Logger LOG = LogManager.getLogger(HrVideoJsonDeserializer.class);

  @Override
  public Optional<HrVideoInfoDto> deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    JsonObject aJsonObject = aJsonElement.getAsJsonObject();

    if (!aJsonObject.has(ELEMENT_VIDEO_RESOLUTION_LEVELS)
            || !aJsonObject.get(ELEMENT_VIDEO_RESOLUTION_LEVELS).isJsonArray()) {

      // try "old" hr video urls
      Optional<String> url = JsonUtils.getAttributeAsString(aJsonObject, ATTRIBUTE_STREAM_URL);
      if (url.isPresent()) {
        return createVideoInfo(url.get());
      }

      LOG.error("Invalid json, no video files found.");
      return Optional.empty();
    }

    List<HrVideoInfo> hrVideoInfoList
            = parseVideoUrls(aJsonObject.get(ELEMENT_VIDEO_RESOLUTION_LEVELS).getAsJsonArray());
    if (hrVideoInfoList.isEmpty()) {
      return Optional.empty();
    }

    final HrVideoInfoDto videoInfoDto = createVideoInfoDto(hrVideoInfoList);

    Optional<String> subtitle = JsonUtils
            .getAttributeAsString(aJsonObject, ATTRIBUTE_SUBTITLE);
    subtitle.ifPresent(videoInfoDto::setSubtitle);

    return Optional.of(videoInfoDto);
  }

  private HrVideoInfoDto createVideoInfoDto(List<HrVideoInfo> hrVideoInfoList) {
    HrVideoInfoDto videoInfoDto = new HrVideoInfoDto();

    for (HrVideoInfo videoInfo : hrVideoInfoList) {
      if (!videoInfoDto.containsQuality(videoInfo.getResolution())) {
        videoInfoDto.addVideo(videoInfo.getResolution(), videoInfo.getUrl());
      }
    }

    return videoInfoDto;
  }

  private List<HrVideoInfo> parseVideoUrls(JsonArray videoArray) {
    final List<HrVideoInfo> videoInfos = new ArrayList<>();

    for (JsonElement videoElement : videoArray) {
      final JsonObject videoObject = videoElement.getAsJsonObject();
      Optional<String> url = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_URL);
      Optional<String> height
              = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_VERTICAL_RESOLUTION);

      if (url.isPresent() && height.isPresent()) {
        videoInfos.add(new HrVideoInfo(Integer.parseInt(height.get()), url.get()));
      }
    }
    videoInfos.sort(Comparator.comparingInt(HrVideoInfo::getHeight).reversed());
    return videoInfos;
  }

  private Optional<HrVideoInfoDto> createVideoInfo(String videoUrl) {
    HrVideoInfoDto dto = new HrVideoInfoDto();
    dto.addVideo(Qualities.NORMAL, videoUrl);

    return Optional.of(dto);
  }

  private class HrVideoInfo {

    private final int height;
    private final String url;
    private final Qualities resolution;

    HrVideoInfo(int height, String url) {
      this.height = height;
      this.url = url;
      this.resolution = getResolutionFromHeight(height);
    }

    private Qualities getResolutionFromHeight(int height) {
      if (height >= 720) {
        return Qualities.HD;
      }
      if (height >= 360) {
        return Qualities.NORMAL;
      }

      return Qualities.SMALL;
    }

    public int getHeight() {
      return height;
    }

    public String getUrl() {
      return url;
    }

    public Qualities getResolution() {
      return resolution;
    }
  }
}
