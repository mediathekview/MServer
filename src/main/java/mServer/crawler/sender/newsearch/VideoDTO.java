package mServer.crawler.sender.newsearch;

/**
 * A  Data-Transfer-Object which contains all needed information for MediathekView.
 */
public class VideoDTO
{
    private String date;
    private String description;
    private int duration;
    private String time;
    private String title;
    private String topic;
    private String websiteUrl;
    private DownloadDTO downloadDto;

    public String getDate() {
        return replaceNull(date);
    }
    
    public void setDate(String aDate) {
        date = aDate;
    }
    
    public String getDescription() {
        return replaceNull(description);
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
    
    public String getTime() {
        return replaceNull(time);
    }
    
    public void setTime(String aTime) {
        time = aTime;
    }
    
    public String getTitle() {
        return replaceNull(title);
    }
    
    public void setTitle(String aTitle) {
        title = aTitle;
    }

    public String getTopic() {
        return replaceNull(topic);
    }
    
    public void setTopic(String aTopic) {
        topic = aTopic;
    }
    
    public String getWebsiteUrl() {
        return replaceNull(websiteUrl);
    }
    
    public void setWebsiteUrl(String aWebsiteUrl) {
        websiteUrl = aWebsiteUrl;
    }
    
    public DownloadDTO getDownloadDto() {
        return downloadDto;
    }
    
    public void setDownloadDto(DownloadDTO aDto) {
        downloadDto = aDto;
    }
    
    private static String replaceNull(String value) {
        if(value != null) {
            return value;
        }
        return "";
    }
}
