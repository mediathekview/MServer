package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.daten.Resolution;
import java.util.HashMap;
import java.util.Map;

public class WdrVideoDto {
    private final Map<Resolution,String> videoUrls;
    private String subtitleUrl = "";
    
    public WdrVideoDto()
    {
        videoUrls=new HashMap<>();
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
