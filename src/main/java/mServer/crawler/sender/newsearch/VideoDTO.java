package mServer.crawler.sender.newsearch;

/**
 * A  Data-Transfer-Object which contains all needed information for MediathekView.
 */
public class VideoDTO
{
    private String description;
    private int duration;
    private String title;
    private String topic;
    private String websiteUrl;

    public String getDescription() {
        return description;
    }
    
    public void setDescription(String aDescription) {
        description = aDescription;
    }
    
    public int getDuration() {
        return duration;
    }
    
    public void setDuration(int aDuration) {
        duration = aDuration;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String aTitle) {
        title = aTitle;
    }

    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String aTopic) {
        topic = aTopic;
    }
    
    public String getWebsiteUrl() {
        return websiteUrl;
    }
    
    public void setWebsiteUrl(String aWebsiteUrl) {
        websiteUrl = aWebsiteUrl;
    }
}
