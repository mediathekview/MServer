package mServer.crawler.sender.zdf.json;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;

/**
 * A data transfer object containing the information for downloading a video.
 */
public class DownloadDto {

  private Optional<GeoLocations> geoLocation;
  private Optional<String> subTitleUrl;
  private final Map<String, Map<Qualities, String>> downloadUrls;

  public DownloadDto() {
    downloadUrls = new HashMap<>();
    geoLocation = Optional.empty();
    subTitleUrl = Optional.empty();
  }

  public void addUrl(final String language, final Qualities quality, final String url) {
    if (!downloadUrls.containsKey(language)) {
      downloadUrls.put(language, new EnumMap<>(Qualities.class));
    }

    Map<Qualities, String> urlMap = downloadUrls.get(language);
    urlMap.put(quality, url);
  }

  public Map<Qualities, String> getDownloadUrls(final String language) {
    if (downloadUrls.containsKey(language)) {
      return downloadUrls.get(language);
    }

    return new EnumMap<>(Qualities.class);
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

  public Optional<String> getUrl(final String language, final Qualities resolution) {
    if (downloadUrls.containsKey(language)) {
      Map<Qualities, String> urlMap = downloadUrls.get(language);
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
