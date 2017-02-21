package mServer.crawler.sender.newsearch;

import java.util.HashMap;

/**
 * A data transfer object containing the information for downloading a video
 */
public class DownloadDTO {

    private GeoLocations geoLocation;
    private String subTitleUrl;
    private final HashMap<Qualities, String> downloadUrls;

    public DownloadDTO() {
        this.downloadUrls = new HashMap<>();
    }

    public void addUrl(Qualities quality, String url) {
        downloadUrls.put(quality, url);
    }
    
    public String getUrl(Qualities quality) {
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
}