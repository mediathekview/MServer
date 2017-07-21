package mServer.crawler.sender.arte;

import java.util.HashMap;
import java.util.Map;

import mServer.crawler.sender.newsearch.Qualities;

public class ArteVideoDTO {
    private final Map<Qualities,String> videoUrls;
    private long durationInSeconds;
    
    public ArteVideoDTO()
    {
        videoUrls=new HashMap<>();
        durationInSeconds = 0;
    }
    
    public void addVideo(Qualities aQualitie, String aUrl)
    {
        videoUrls.put(aQualitie,aUrl);
    }
    
    public Map<Qualities,String> getVideoUrls()
    {
        return videoUrls;
    }
    
    public String getUrl(Qualities aQualitie)
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
