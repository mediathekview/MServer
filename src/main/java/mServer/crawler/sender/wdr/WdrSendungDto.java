package mServer.crawler.sender.wdr;

import java.util.ArrayList;
import java.util.List;

public class WdrSendungDto {

    private String theme;
    private final List<String> videoUrls;
    private final List<String> overviewUrls;

    public WdrSendungDto() {
        videoUrls = new ArrayList<>();
        overviewUrls = new ArrayList<>();
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String aTheme) {
        theme = aTheme;
    }

    public List<String> getOverviewUrls() {
        return overviewUrls;
    }
    
    public void addOverviewUrls(String aUrl) {
        if(aUrl != null && !aUrl.isEmpty()) {
            overviewUrls.add(aUrl);
        }
    }
    
    public List<String> getVideoUrls() {
        return videoUrls;
    }
    
    public void addVideoUrl(String aUrl) {
        if(aUrl != null && !aUrl.isEmpty()) {
            videoUrls.add(aUrl);
        }
    }
}
