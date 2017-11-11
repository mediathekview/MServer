package mServer.crawler.sender.arte;

import de.mediathekview.mlib.daten.Resolution;

import java.util.HashMap;
import java.util.Map;


public class ArteVideoDTO {
    private Map<Resolution,String> videoUrls;
    private long durationInSeconds;
    
    public ArteVideoDTO()
    {
        videoUrls=new HashMap<>();
        durationInSeconds = 0;
    }
    
    public void addVideo(Resolution aQualitie, String aUrl)
    {
        videoUrls.put(aQualitie,aUrl);
    }
    
    public Map<Resolution,String> getVideoUrls()
    {
        return videoUrls;
    }
    
    public String getUrl(Resolution aQualitie)
    {
        return videoUrls.get(aQualitie);
    }
    
    public void setDurationInSeconds(long durationInSeconds) {
        this.durationInSeconds = durationInSeconds;
    }
    
    public long getDurationInSeconds() {
        return durationInSeconds;
    }
}
