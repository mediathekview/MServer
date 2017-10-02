package de.mediathekview.mserver.crawler.zdf.json;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;

import java.util.HashMap;

/**
 * A data transfer object containing the information for downloading a video
 */
public class DownloadDTO {

    private GeoLocations geoLocation;
    private String subTitleUrl;
    private final HashMap<Resolution, String> downloadUrls;

    public DownloadDTO() {
        this.downloadUrls = new HashMap<>();
    }

    public void addUrl(Resolution quality, String url) {
        downloadUrls.put(quality, url);
    }
    
    public String getUrl(Resolution quality) {
        String url = downloadUrls.get(quality);
        if(url == null) {
            return "";
        }
        return url;
    }
    
    public GeoLocations getGeoLocation() {
        if(geoLocation == null) {
            return GeoLocations.GEO_NONE;
        }        
        return geoLocation;
    }
    
    public void setGeoLocation(GeoLocations aGeoLocation) {
        geoLocation = aGeoLocation;
    }
    
    public String getSubTitleUrl() {
        if(subTitleUrl == null) {
            return "";
        }
        return subTitleUrl;
    }
    
    public void setSubTitleUrl(String aUrl) {
        subTitleUrl = aUrl;
    }

    public boolean hasUrl(final Resolution aQuality)
    {
        return  downloadUrls.containsKey(aQuality);
    }
}