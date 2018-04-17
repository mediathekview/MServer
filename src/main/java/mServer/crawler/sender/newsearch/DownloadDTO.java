package mServer.crawler.sender.newsearch;

import java.util.HashMap;
import java.util.Set;

/**
 * A data transfer object containing the information for downloading a video
 */
public class DownloadDTO {

  public static final String LANGUAGE_ENGLISH = "eng";
  public static final String LANGUAGE_GERMAN = "deu";

  private GeoLocations geoLocation;
  private String subTitleUrl;
  private final HashMap<String, HashMap<Qualities, String>> downloadUrls;

  public DownloadDTO() {
    this.downloadUrls = new HashMap<>();
  }

  public void addUrl(String language, Qualities quality, String url) {
    if (!downloadUrls.containsKey(language)) {
      downloadUrls.put(language, new HashMap<>());
    }

    HashMap<Qualities, String> urlMap = downloadUrls.get(language);
    urlMap.put(quality, url);
  }

  public Set<String> getLanguages() {
    return downloadUrls.keySet();
  }

  public String getUrl(String language, Qualities quality) {
    HashMap<Qualities, String> urlMap = downloadUrls.get(language);
    if (urlMap == null) {
      return "";
    }

    String url = urlMap.get(quality);
    if (url == null) {
      return "";
    }
    return url;
  }

  public GeoLocations getGeoLocation() {
    if (geoLocation == null) {
      return GeoLocations.GEO_NONE;
    }
    return geoLocation;
  }

  public void setGeoLocation(GeoLocations aGeoLocation) {
    geoLocation = aGeoLocation;
  }

  public String getSubTitleUrl() {
    if (subTitleUrl == null) {
      return "";
    }
    return subTitleUrl;
  }

  public void setSubTitleUrl(String aUrl) {
    subTitleUrl = aUrl;
  }
}
