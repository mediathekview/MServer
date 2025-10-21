package de.mediathekview.mserver.crawler.zdf.json;

import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Resolution;

import java.time.Duration;
import java.util.*;

/** A data transfer object containing the information for downloading a video. */
public class DownloadDto {

  private Optional<GeoLocations> geoLocation;
  private final Map<String, String> subTitleUrls;
  private final Map<String, Map<Resolution, String>> downloadUrls;
  private Optional<Duration> duration;

  public DownloadDto() {
    downloadUrls = new HashMap<>();
    geoLocation = Optional.empty();
    subTitleUrls = new HashMap<>();
    duration = Optional.empty();
  }

  public void addUrl(final String language, final Resolution quality, final String url) {
    downloadUrls.computeIfAbsent(language, k -> (new EnumMap<>(Resolution.class))).put(quality, url);
  }

  public Map<Resolution, String> getDownloadUrls(final String language) {
    if (downloadUrls.containsKey(language)) {
      return downloadUrls.get(language);
    }

    return new EnumMap<>(Resolution.class);
  }

  public Optional<Duration> getDuration() {
    return duration;
  }

  public Set<String> getLanguages() {
    return downloadUrls.keySet();
  }

  public Optional<GeoLocations> getGeoLocation() {
    return geoLocation;
  }

  public Optional<String> getSubTitleUrl(String language) {
    if (subTitleUrls.containsKey(language)) {
      return Optional.of(subTitleUrls.get(language));
    }
    return Optional.empty();
  }

  public Optional<String> getUrl(final String language, final Resolution resolution) {
    if (downloadUrls.containsKey(language)) {
      Map<Resolution, String> urlMap = downloadUrls.get(language);
      if (urlMap.containsKey(resolution)) {
        return Optional.of(urlMap.get(resolution));
      }
    }
    return Optional.empty();
  }

  public void setGeoLocation(final GeoLocations aGeoLocation) {
    geoLocation = Optional.of(aGeoLocation);
  }

  public void addSubTitleUrl(final String language, final String aUrl) {
    subTitleUrls.put(language, aUrl);
  }

  public void setDuration(final Duration duration) {
    this.duration = Optional.of(duration);
  }
}
