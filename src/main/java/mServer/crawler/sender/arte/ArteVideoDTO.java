package mServer.crawler.sender.arte;

import java.util.HashMap;
import java.util.Map;

import mServer.crawler.sender.base.Qualities;

public class ArteVideoDTO {

  private final Map<Qualities, String> videoUrls;
  private final Map<Qualities, String> videoUrlsWithSubtitle;
  private final Map<Qualities, String> videoUrlsWithAudioDescription;
  private long durationInSeconds;

  public ArteVideoDTO() {
    videoUrls = new HashMap<>();
    videoUrlsWithSubtitle = new HashMap<>();
    videoUrlsWithAudioDescription = new HashMap<>();
    durationInSeconds = 0;
  }

  public void addVideo(Qualities aQualitie, String aUrl) {
    videoUrls.put(aQualitie, aUrl);
  }

  public void addVideoWithAudioDescription(Qualities qualities, String url) {
    videoUrlsWithAudioDescription.put(qualities, url);
  }

  public void addVideoWithSubtitle(Qualities qualities, String url) {
    videoUrlsWithSubtitle.put(qualities, url);
  }

  public Map<Qualities, String> getVideoUrls() {
    return videoUrls;
  }

  public Map<Qualities, String> getVideoUrlsWithAudioDescription() {
    return videoUrlsWithAudioDescription;
  }

  public Map<Qualities, String> getVideoUrlsWithSubtitle() {
    return videoUrlsWithSubtitle;
  }

  public void setDurationInSeconds(long durationInSeconds) {
    this.durationInSeconds = durationInSeconds;
  }

  public long getDurationInSeconds() {
    return durationInSeconds;
  }
}
