package mServer.crawler.sender.newsearch;

import java.util.HashMap;

/**
 * A data transfer object containing the information for downloading a video
 */
public class DownloadDTO {

    private String subTitleUrl;
    private final HashMap<Qualities, String> downloadUrls;

    public DownloadDTO() {
        this.downloadUrls = new HashMap<>();
    }

    public void AddUrl(Qualities quality, String url) {
        downloadUrls.put(quality, url);
    }
    
    public String GetUrl(Qualities quality) {
        return downloadUrls.get(quality);
    }
    
    public String GetSubTitleUrl() {
        if(subTitleUrl == null) {
            return "";
        }
        return subTitleUrl;
    }
    
    public void SetSubTitleUrl(String aUrl) {
        subTitleUrl = aUrl;
    }
}