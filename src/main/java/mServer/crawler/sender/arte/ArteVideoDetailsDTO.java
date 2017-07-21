package mServer.crawler.sender.arte;

import mServer.crawler.sender.newsearch.GeoLocations;

public class ArteVideoDetailsDTO {
    private String broadcastBegin = "";
    private GeoLocations geo = GeoLocations.GEO_NONE;

    private String title = "";
    private String theme = "";
    private String description = "";
    private String website = "";

    public String getBroadcastBegin() {
        return this.broadcastBegin;
    }
    
    public void setBroadcastBegin(String aBroadcastBegin) {
        this.broadcastBegin = aBroadcastBegin;
    }
    
    public GeoLocations getGeoLocation() {
        return this.geo;
    }
    
    public void setGeoLocation(GeoLocations aGeo) {
        this.geo = aGeo;
    }
    
    public String getTitle() {
        return title;
    }
    public void setTitle(String aTitle) {
        title = aTitle;
    }
    public void setTheme(String aTheme) {
        theme = aTheme;
    }
    public void setDescription(String aDescription) {
        description = aDescription;
    }
    public void setWebsite(String aWebsite) {
        website = aWebsite;
    }
    public String getTheme() {
        return theme;
    }
    public String getDescription() {
        return description;
    }
    public String getWebsite() {
        return website;
    }
}
