package de.mediathekview.mserver.crawler.ard.json;

import de.mediathekview.mlib.daten.Resolution;
import org.apache.commons.lang3.StringUtils;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Video information from {@literal
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash}.
 */
public class ArdVideoInfoDto {

  private final Map<Resolution, String> videoUrls;
  private String subtitleUrl;

  public ArdVideoInfoDto() {
    videoUrls = new EnumMap<>(Resolution.class);
  }

  public Resolution getDefaultQuality() {
    if (videoUrls.containsKey(Resolution.NORMAL)) {
      return Resolution.NORMAL;
    }
    return videoUrls.keySet().iterator().next();
  }

  public String getDefaultVideoUrl() {
    return videoUrls.get(getDefaultQuality());
  }

  public String getSubtitleUrl() {
    return subtitleUrl;
  }

  public void setSubtitleUrl(final String subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }

  public Optional<String> getSubtitleUrlOptional() {
    if (StringUtils.isNotBlank(subtitleUrl)) {
      return Optional.of(subtitleUrl);
    }

    return Optional.empty();
  }

  public Map<Resolution, String> getVideoUrls() {
    return videoUrls;
  }

  public boolean containsResolution(final Resolution key) {
    return videoUrls.containsKey(key);
  }

  public String put(final Resolution key, final String value) {
    return videoUrls.put(key, value);
  }
}
