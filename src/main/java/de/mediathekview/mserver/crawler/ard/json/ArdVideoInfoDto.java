package de.mediathekview.mserver.crawler.ard.json;

import de.mediathekview.mlib.daten.Resolution;
import java.util.EnumMap;
import java.util.Map;

/**
 * Video information from http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash.
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

  public Map<Resolution, String> getVideoUrls() {
    return videoUrls;
  }

  public String put(final Resolution key, final String value) {
    return videoUrls.put(key, value);
  }

  public void setSubtitleUrl(final String subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }

}
