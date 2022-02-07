package mServer.crawler.sender.arte;

import mServer.crawler.sender.base.Qualities;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public class ArteVideoDTO {

  private Duration duration;
  private final Map<Qualities, String> videoUrls;
  private final Map<Qualities, String> videoUrlsWithSubtitle;
  private final Map<Qualities, String> videoUrlsWithAudioDescription;
  private final Map<Qualities, String> videoUrlsOriginal;
  private final Map<Qualities, String> videoUrlsOriginalWithSubtitle;

  public ArteVideoDTO() {
    videoUrls = new EnumMap<>(Qualities.class);
    videoUrlsWithSubtitle = new EnumMap<>(Qualities.class);
    videoUrlsWithAudioDescription = new EnumMap<>(Qualities.class);
    videoUrlsOriginal = new EnumMap<>(Qualities.class);
    videoUrlsOriginalWithSubtitle = new EnumMap<>(Qualities.class);
    duration = Duration.ZERO;
  }

  public void addVideo(Qualities aQualities, String aUrl) {
    videoUrls.put(aQualities, aUrl);
  }

  public void addVideoWithAudioDescription(Qualities qualities, String url) {
    videoUrlsWithAudioDescription.put(qualities, url);
  }

  public void addVideoWithSubtitle(Qualities qualities, String url) {
    videoUrlsWithSubtitle.put(qualities, url);
  }
  public void addVideoOriginal(Qualities qualities, String url) {
    videoUrlsOriginal.put(qualities, url);
  }
  public void addVideoOriginalWithSubtitle(Qualities qualities, String url) {
    videoUrlsOriginalWithSubtitle.put(qualities, url);
  }

  public Duration getDuration() { return duration; }

  public Map<Qualities, String> getVideoUrls() {
    return videoUrls;
  }

  public Map<Qualities, String> getVideoUrlsWithAudioDescription() {
    return videoUrlsWithAudioDescription;
  }

  public Map<Qualities, String> getVideoUrlsWithSubtitle() {
    return videoUrlsWithSubtitle;
  }

  public Map<Qualities, String> getVideoUrlsOriginal() {
    return videoUrlsOriginal;
  }

  public Map<Qualities, String> getVideoUrlsOriginalWithSubtitle() {
    return videoUrlsOriginalWithSubtitle;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }
}
