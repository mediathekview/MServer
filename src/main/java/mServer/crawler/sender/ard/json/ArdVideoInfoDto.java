package mServer.crawler.sender.ard.json;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.sender.newsearch.Qualities;
import org.apache.commons.lang3.StringUtils;

/**
 * Video information from
 * {@literal http://www.ardmediathek.de/play/media/[documentId]?devicetype=pc&features=flash}.
 */
public class ArdVideoInfoDto {

  private final Map<Qualities, String> videoUrls;
  private String subtitleUrl;

  public ArdVideoInfoDto() {
    videoUrls = new EnumMap<>(Qualities.class);
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

  public Optional<String> getSubtitleUrlOptional() {
    if (StringUtils.isNotBlank(subtitleUrl)) {
      return Optional.of(subtitleUrl);
    }

    return Optional.empty();
  }

  public Map<Qualities, String> getVideoUrls() {
    return videoUrls;
  }

  public boolean containsQualities(final Qualities key) {
    return videoUrls.containsKey(key);
  }

  public String put(final Qualities key, final String value) {
    return videoUrls.put(key, value);
  }

  public void setSubtitleUrl(final String subtitleUrl) {
    this.subtitleUrl = subtitleUrl;
  }

}
