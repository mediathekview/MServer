package de.mediathekview.mserver.crawler.hr.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.json.ArdVideoInfoDto;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class HrVideoJsonDeserializer implements JsonDeserializer<Optional<ArdVideoInfoDto>> {

  private static final String ATTRIBUTE_STREAM_URL = "streamUrl";
  private static final String ATTRIBUTE_SUBTITLE = "subtitle";
  private static final String ATTRIBUTE_URL = "url";
  private static final String ATTRIBUTE_VERTICAL_RESOLUTION = "verticalResolution";
  private static final String ELEMENT_VIDEO_RESOLUTION_LEVELS = "videoResolutionLevels";

  private static final Logger LOG = LogManager.getLogger(HrVideoJsonDeserializer.class);

  @Override
  public Optional<ArdVideoInfoDto> deserialize(
      JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

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

    List<HrVideoInfo> hrVideoInfoList =
        parseVideoUrls(aJsonObject.get(ELEMENT_VIDEO_RESOLUTION_LEVELS).getAsJsonArray());
    if (hrVideoInfoList.isEmpty()) {
      return Optional.empty();
    }


    final ArdVideoInfoDto videoInfoDto = createVideoInfoDto(hrVideoInfoList);

    Optional<String> subtitle = JsonUtils
        .getAttributeAsString(aJsonObject, ATTRIBUTE_SUBTITLE);
    subtitle.ifPresent(videoInfoDto::setSubtitleUrl);

    return Optional.of(videoInfoDto);
  }

  private Optional<ArdVideoInfoDto> createVideoInfo(String videoUrl) {
    ArdVideoInfoDto dto = new ArdVideoInfoDto();
    dto.put(Resolution.NORMAL, videoUrl);

    return Optional.of(dto);
  }

  private ArdVideoInfoDto createVideoInfoDto(List<HrVideoInfo> hrVideoInfoList) {
    ArdVideoInfoDto videoInfoDto = new ArdVideoInfoDto();

    for (HrVideoInfo videoInfo : hrVideoInfoList) {
      if (!videoInfoDto.containsResolution(videoInfo.getResolution())) {
        videoInfoDto.put(videoInfo.getResolution(), videoInfo.getUrl());
      }
    }

    return videoInfoDto;
  }

  private List<HrVideoInfo> parseVideoUrls(JsonArray videoArray) {
    final List<HrVideoInfo> videoInfos = new ArrayList<>();

    for (JsonElement videoElement : videoArray) {
      final JsonObject videoObject = videoElement.getAsJsonObject();
      Optional<String> url = JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_URL);
      Optional<String> height =
          JsonUtils.getAttributeAsString(videoObject, ATTRIBUTE_VERTICAL_RESOLUTION);

      if (url.isPresent() && height.isPresent()) {
        videoInfos.add(new HrVideoInfo(Integer.parseInt(height.get()), url.get()));
      }
    }

    videoInfos.sort(Comparator.comparingInt(HrVideoInfo::getHeight).reversed());
    return videoInfos;
  }

  private class HrVideoInfo {
    private final int height;
    private final String url;
    private final Resolution resolution;

    HrVideoInfo(int height, String url) {
      this.height = height;
      this.url = url;
      this.resolution = getResolutionFromHeight(height);
    }

    private Resolution getResolutionFromHeight(int height) {
      if (height >= 720) {
        return Resolution.HD;
      }
      if (height >= 360) {
        return Resolution.NORMAL;
      }
      if (height >= 180) {
        return Resolution.SMALL;
      }

      return Resolution.VERY_SMALL;
    }

    public int getHeight() {
      return height;
    }

    public String getUrl() {
      return url;
    }

    public Resolution getResolution() {
      return resolution;
    }
  }
}
