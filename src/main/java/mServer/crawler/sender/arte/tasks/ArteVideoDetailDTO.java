package mServer.crawler.sender.arte.tasks;

import mServer.crawler.sender.base.Qualities;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public class ArteVideoDetailDTO {

  private Duration duration;
  private final Map<Qualities, String> urls;
  private final Map<Qualities, String> urlsWithSubtitle;
  private final Map<Qualities, String> urlsAudioDescription;
  private final Map<Qualities, String> urlsOriginalWithSubtitle;
  private final Map<Qualities, String> urlsOriginal;

  public ArteVideoDetailDTO() {
    urls = new EnumMap<>(Qualities.class);
    urlsWithSubtitle = new EnumMap<>(Qualities.class);
    urlsAudioDescription = new EnumMap<>(Qualities.class);
    urlsOriginalWithSubtitle = new EnumMap<>(Qualities.class);
    urlsOriginal = new EnumMap<>(Qualities.class);
  }

  public String get(final Object aKey) {
    return urls.get(aKey);
  }

  public Duration getDuration() { return duration; }

  public String getSubtitle(final Object aKey) {
    return urlsWithSubtitle.get(aKey);
  }

  public String getOriginalWithSubtitle(final Object aKey) {
    return urlsOriginalWithSubtitle.get(aKey);
  }

  public String getAudioDescription(final Object aKey) {
    return urlsAudioDescription.get(aKey);
  }

  public String getOriginal(final Object aKey) {
    return urlsOriginal.get(aKey);
  }

  public Map<Qualities, String> getUrls() {
    return new EnumMap<>(urls);
  }

  public Map<Qualities, String> getUrlsWithSubtitle() {
    return new EnumMap<>(urlsWithSubtitle);
  }

  public Map<Qualities, String> getUrlsAudioDescription() {
    return new EnumMap<>(urlsAudioDescription);
  }

  public Map<Qualities, String> getUrlsOriginalWithSubtitle() {
    return new EnumMap<>(urlsOriginalWithSubtitle);
  }

  public Map<Qualities, String> getUrlsOriginal() {
    return new EnumMap<>(urlsOriginal);
  }

  public void setDuration(final Duration duration) { this.duration = duration; }

  public String put(final Qualities aResolution, final String aUrl) {
    return urls.put(aResolution, aUrl);
  }

  public String putSubtitle(final Qualities aResolution, final String aUrl) {
    return urlsWithSubtitle.put(aResolution, aUrl);
  }

  public String putAudioDescription(final Qualities aResolution, final String aUrl) {
    return urlsAudioDescription.put(aResolution, aUrl);
  }

  public String putOriginal(final Qualities aResolution, final String aUrl) {
    return urlsOriginal.put(aResolution, aUrl);
  }

  public String putOriginalWithSubtitle(final Qualities aResolution, final String aUrl) {
    return urlsOriginalWithSubtitle.put(aResolution, aUrl);
  }
}
