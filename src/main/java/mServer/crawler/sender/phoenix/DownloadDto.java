package mServer.crawler.sender.phoenix;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.crawler.sender.newsearch.Qualities;

/**
 * A data transfer object containing the information for downloading a video.
 */
public class DownloadDto {

  private Optional<GeoLocations> geoLocation;
  private Optional<String> subTitleUrl;
  private final Map<Qualities, String> downloadUrls;

  public DownloadDto() {
    downloadUrls = new EnumMap<>(Qualities.class);
    geoLocation = Optional.empty();
    subTitleUrl = Optional.empty();
  }

  public void addUrl(final Qualities quality, final String url) {
    downloadUrls.put(quality, url);
  }

  public Map<Qualities, String> getDownloadUrls() {
    return downloadUrls;
  }

  public Optional<GeoLocations> getGeoLocation() {
    return geoLocation;
  }

  public Optional<String> getSubTitleUrl() {
    return subTitleUrl;
  }

  public Optional<String> getUrl(final Qualities resolution) {
    if (downloadUrls.containsKey(resolution)) {
      return Optional.of(downloadUrls.get(resolution));
    }
    return Optional.empty();
  }

  public boolean hasUrl(final Qualities aQuality) {
    return downloadUrls.containsKey(aQuality);
  }

  public void setGeoLocation(final GeoLocations aGeoLocation) {
    geoLocation = Optional.of(aGeoLocation);
  }

  public void setSubTitleUrl(final String aUrl) {
    subTitleUrl = Optional.of(aUrl);
  }
}
