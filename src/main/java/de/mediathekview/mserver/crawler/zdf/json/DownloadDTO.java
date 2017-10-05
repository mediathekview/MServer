package de.mediathekview.mserver.crawler.zdf.json;

import java.util.HashMap;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;

/**
 * A data transfer object containing the information for downloading a video
 */
public class DownloadDTO {

  private GeoLocations geoLocation;
  private String subTitleUrl;
  private final HashMap<Resolution, String> downloadUrls;

  public DownloadDTO() {
    downloadUrls = new HashMap<>();
  }

  public void addUrl(final Resolution quality, final String url) {
    downloadUrls.put(quality, url);
  }

  public HashMap<Resolution, String> getDownloadUrls() {
    return downloadUrls;
  }

  public GeoLocations getGeoLocation() {
    if (geoLocation == null) {
      return GeoLocations.GEO_NONE;
    }
    return geoLocation;
  }

  public String getSubTitleUrl() {
    if (subTitleUrl == null) {
      return "";
    }
    return subTitleUrl;
  }

  public String getUrl(final Resolution quality) {
    final String url = downloadUrls.get(quality);
    if (url == null) {
      return "";
    }
    return url;
  }

  public boolean hasUrl(final Resolution aQuality) {
    return downloadUrls.containsKey(aQuality);
  }

  public void setGeoLocation(final GeoLocations aGeoLocation) {
    geoLocation = aGeoLocation;
  }

  public void setSubTitleUrl(final String aUrl) {
    subTitleUrl = aUrl;
  }
}
