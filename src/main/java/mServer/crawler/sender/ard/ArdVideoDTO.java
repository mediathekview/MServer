package mServer.crawler.sender.ard;

import java.util.HashMap;
import java.util.Map;
import de.mediathekview.mlib.daten.Qualities;

public class ArdVideoDTO {
    private final Map<Qualities,String> videoUrls;

    public ArdVideoDTO() {
        videoUrls = new HashMap<>();
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
}
