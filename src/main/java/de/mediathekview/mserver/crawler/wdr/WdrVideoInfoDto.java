package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mlib.daten.Resolution;
import java.util.EnumMap;
import java.util.Map;

public class WdrVideoInfoDto {
  private final Map<Resolution, String> audioDescriptionUrls;
  private final Map<Resolution, String> signLanguageUrls;
  private final Map<Resolution, String> videoUrls;
  private String subtitleUrl;

  public WdrVideoInfoDto() {
    audioDescriptionUrls = new EnumMap<>(Resolution.class);
    signLanguageUrls = new EnumMap<>(Resolution.class);
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

  public Map<Resolution, String> getAudioDescriptionUrls() {
    return audioDescriptionUrls;
  }

  public String putAudioDescription(final Resolution key, final String value) {
    return audioDescriptionUrls.put(key, value);
  }

  public Map<Resolution, String> getSignLanguageUrls() {
    return signLanguageUrls;
  }

  public String putSignLanguage(final Resolution key, final String value) {
    return signLanguageUrls.put(key, value);
  }

  public Map<Resolution, String> getVideoUrls() {
    return videoUrls;
  }

  public String putVideo(final Resolution key, final String value) {
    return videoUrls.put(key, value);
  }

  public void setSubtitleUrl(final String subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }
}
