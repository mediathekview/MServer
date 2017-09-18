package mServer.crawler.sender.dw;

import java.util.HashMap;
import java.util.Map;
import mServer.crawler.sender.newsearch.Qualities;

public class DwVideoDTO {

    private final Map<Qualities,String> videoUrls;
    
    public DwVideoDTO() {
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
        if (url == null) {
            return "";
        }
        return url;
    }
}
