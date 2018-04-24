package de.mediathekview.mserver.crawler.zdf.json;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A data transfer object containing the information for downloading a video.
 */
public class DownloadDto {

  private Optional<GeoLocations> geoLocation;
  private Optional<String> subTitleUrl;
  private final Map<String, Map<Resolution, String>> downloadUrls;

  public DownloadDto() {
    downloadUrls = new HashMap<>();
    geoLocation = Optional.empty();
    subTitleUrl = Optional.empty();
  }

  public void addUrl(final String language, final Resolution quality, final String url) {
    if (!downloadUrls.containsKey(language)) {
      downloadUrls.put(language, new EnumMap<>(Resolution.class));
    }

    Map<Resolution, String> urlMap = downloadUrls.get(language);
    urlMap.put(quality, url);
  }

  public Map<Resolution, String> getDownloadUrls(final String language) {
    if (downloadUrls.containsKey(language)) {
      return downloadUrls.get(language);
    }

    return new EnumMap<>(Resolution.class);
  }

  public Set<String> getLanguages() {
    return downloadUrls.keySet();
  }

  public Optional<GeoLocations> getGeoLocation() {
    return geoLocation;
  }

  public Optional<String> getSubTitleUrl() {
    return subTitleUrl;
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

  public void setSubTitleUrl(final String aUrl) {
    subTitleUrl = Optional.of(aUrl);
  }
}
