package de.mediathekview.mserver.crawler.orf;

import java.util.EnumMap;
import java.util.Map;
import de.mediathekview.mlib.daten.Resolution;

public class OrfVideoInfoDTO {

  private static final String FILTER_JUGENDSCHUTZ = ".*/Jugendschutz[0-9][0-9][0-9][0-9]b[0-9][0-9][0-9][0-9]_.*";
  private final Map<Resolution, String> videoUrls;
  private String subtitleUrl;

  public OrfVideoInfoDTO() {
    videoUrls = new EnumMap<>(Resolution.class);
  }

  public boolean hasVideoUrls() {
    return !videoUrls.isEmpty();
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
    if (value == null || value.matches(FILTER_JUGENDSCHUTZ)) {
      return "";
    }
    return videoUrls.put(key, value);
  }

  public void setSubtitleUrl(final String subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }
}