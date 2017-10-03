package mServer.crawler.sender.wdr;

import java.util.ArrayList;
import java.util.List;

public class WdrSendungOverviewDto {

    private String theme;
    private List<String> urls;

    public WdrSendungOverviewDto() {
        urls = new ArrayList<>();
    }
    
    public String getTheme() {
        return theme;
    }
    
    public void setTheme(String aTheme) {
        theme = aTheme;
    }

    public List<String> getUrls() {
        return urls;
    }
    
    public void addUrl(String aUrl) {
        if(aUrl != null && !aUrl.isEmpty()) {
            urls.add(aUrl);
        }
    }
}
