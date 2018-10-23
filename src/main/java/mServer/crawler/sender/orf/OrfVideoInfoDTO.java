package mServer.crawler.sender.orf;

import java.util.EnumMap;
import java.util.Map;
import mServer.crawler.sender.newsearch.Qualities;

public class OrfVideoInfoDTO {

  private final Map<Qualities, String> videoUrls;
  private String subtitleUrl;

  public OrfVideoInfoDTO() {
    videoUrls = new EnumMap<>(Qualities.class);
  }

  public boolean hasVideoUrls() {
    return !videoUrls.isEmpty();
  }

  public Qualities getDefaultQuality() {
    if (videoUrls.containsKey(Qualities.NORMAL)) {
      return Qualities.NORMAL;
    }
    return videoUrls.keySet().iterator().next();
  }

  public String getDefaultVideoUrl() {
    return videoUrls.get(getDefaultQuality());
  }

  public String getSubtitleUrl() {
    return subtitleUrl;
  }

  public Map<Qualities, String> getVideoUrls() {
    return videoUrls;
  }

  public String put(final Qualities key, final String value) {
    return videoUrls.put(key, value);
  }

  public void setSubtitleUrl(final String subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }
}
