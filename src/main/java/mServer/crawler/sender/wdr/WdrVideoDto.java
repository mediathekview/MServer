package mServer.crawler.sender.wdr;

import java.util.HashMap;
import java.util.Map;

import mServer.crawler.sender.newsearch.Qualities;

public class WdrVideoDto {
    private final Map<Qualities,String> videoUrls;
    private String subtitleUrl = "";
    
    public WdrVideoDto()
    {
        videoUrls=new HashMap<>();
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
        String url = videoUrls.get(aQualitie);
        if(url != null) {
            return url;
        }
        
        return "";
    }
    
    public void setSubtitleUrl(String aUrl) {
        this.subtitleUrl = aUrl;
    }
    
    public String getSubtitleUrl() {
        return subtitleUrl;
    }
}
