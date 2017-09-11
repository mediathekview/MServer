package mServer.crawler.sender.arte;
import java.time.LocalDateTime;
import de.mediathekview.mlib.daten.GeoLocations;


public class ArteVideoDetailsDTO {

    private String title = "";
    private String theme = "";
    private String description = "";
    private String website = "";

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
