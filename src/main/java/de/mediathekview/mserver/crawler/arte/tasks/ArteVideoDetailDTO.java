package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mserver.daten.Resolution;

import java.time.Duration;
import java.util.EnumMap;
import java.util.Map;

public class ArteVideoDetailDTO {

  private Duration duration;
  private final Map<Resolution, String> urls;
  private final Map<Resolution, String> urlsWithSubtitle;
  private final Map<Resolution, String> urlsAudioDescription;
  private final Map<Resolution, String> urlsOriginalWithSubtitle;
  private final Map<Resolution, String> urlsOriginal;

  public ArteVideoDetailDTO() {
    urls = new EnumMap<>(Resolution.class);
    urlsWithSubtitle = new EnumMap<>(Resolution.class);
    urlsAudioDescription = new EnumMap<>(Resolution.class);
    urlsOriginalWithSubtitle = new EnumMap<>(Resolution.class);
    urlsOriginal = new EnumMap<>(Resolution.class);
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

  public Map<Resolution, String> getUrls() {
    return new EnumMap<>(urls);
  }

  public Map<Resolution, String> getUrlsWithSubtitle() {
    return new EnumMap<>(urlsWithSubtitle);
  }

  public Map<Resolution, String> getUrlsAudioDescription() {
    return new EnumMap<>(urlsAudioDescription);
  }

  public Map<Resolution, String> getUrlsOriginalWithSubtitle() {
    return new EnumMap<>(urlsOriginalWithSubtitle);
  }

  public Map<Resolution, String> getUrlsOriginal() {
    return new EnumMap<>(urlsOriginal);
  }

  public void setDuration(final Duration duration) { this.duration = duration; }

  public String put(final Resolution aResolution, final String aUrl) {
    return urls.put(aResolution, aUrl);
  }

  public String putSubtitle(final Resolution aResolution, final String aUrl) {
    return urlsWithSubtitle.put(aResolution, aUrl);
  }

  public String putAudioDescription(final Resolution aResolution, final String aUrl) {
    return urlsAudioDescription.put(aResolution, aUrl);
  }

  public String putOriginal(final Resolution aResolution, final String aUrl) {
    return urlsOriginal.put(aResolution, aUrl);
  }

  public String putOriginalWithSubtitle(final Resolution aResolution, final String aUrl) {
    return urlsOriginalWithSubtitle.put(aResolution, aUrl);
  }
}
