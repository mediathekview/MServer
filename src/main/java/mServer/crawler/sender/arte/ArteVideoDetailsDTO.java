package mServer.crawler.sender.arte;
import java.time.LocalDateTime;
import de.mediathekview.mlib.daten.GeoLocations;


public class ArteVideoDetailsDTO {
    private LocalDateTime broadcastBegin;;
    private GeoLocations geoLocation;
    
    public ArteVideoDetailsDTO() {
        geoLocation = GeoLocations.GEO_NONE;
    }
    
    public LocalDateTime getBroadcastBegin() {
        return broadcastBegin;
    }
    
    public void setBroadcastBegin(LocalDateTime aBroadcastBegin) {
        broadcastBegin = aBroadcastBegin;
    }
    
    public GeoLocations getGeoLocation() {
        return geoLocation;
    }
    
    public void setGeoLocation(GeoLocations aGeoLocation) {
        geoLocation = aGeoLocation;
    }
}
