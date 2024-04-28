package de.mediathekview.mserver.crawler.ard.json;

import de.mediathekview.mlib.daten.Resolution;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Video information from {@literal
 * http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash}.
 */
public class ArdVideoInfoDto {

  private final Map<Resolution, String> videoUrls;
  
  private Optional<Set<String>> subtitleUrl;

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

  public Optional<Set<String>> getSubtitleUrl() {
    return subtitleUrl;
  }

  public void setSubtitleUrl(final Optional<Set<String>> subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }

  public Optional<Set<String>> getSubtitleUrlOptional() {
    return subtitleUrl;
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
