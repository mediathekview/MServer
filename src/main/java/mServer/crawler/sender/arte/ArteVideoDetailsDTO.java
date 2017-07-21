package mServer.crawler.sender.arte;

import mServer.crawler.sender.newsearch.GeoLocations;

public class ArteVideoDetailsDTO {
    private String broadcastBegin = "";
    private GeoLocations geo = GeoLocations.GEO_NONE;
    
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
}
