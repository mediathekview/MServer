package de.mediathekview.mserver.crawler.zdf.json;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * A data transfer object containing the information for downloading a video.
 */
public class DownloadDto {

  private Optional<GeoLocations> geoLocation;
  private Optional<String> subTitleUrl;
  private final Map<Resolution, String> downloadUrls;

  public DownloadDto() {
    downloadUrls = new EnumMap<>(Resolution.class);
    geoLocation = Optional.empty();
    subTitleUrl = Optional.empty();
  }

  public void addUrl(final Resolution quality, final String url) {
    downloadUrls.put(quality, url);
  }

  public Map<Resolution, String> getDownloadUrls() {
    return downloadUrls;
  }

  public Optional<GeoLocations> getGeoLocation() {
    return geoLocation;
  }

  public Optional<String> getSubTitleUrl() {
    return subTitleUrl;
  }

  public Optional<String> getUrl(final Resolution resolution) {
    if (downloadUrls.containsKey(resolution)) {
      return Optional.of(downloadUrls.get(resolution));
    }
    return Optional.empty();
  }

  public boolean hasUrl(final Resolution aQuality) {
    return downloadUrls.containsKey(aQuality);
  }

  public void setGeoLocation(final GeoLocations aGeoLocation) {
    geoLocation = Optional.of(aGeoLocation);
  }

  public void setSubTitleUrl(final String aUrl) {
    subTitleUrl = Optional.of(aUrl);
  }
}
