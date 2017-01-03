package mServer.crawler.sender.newsearch;

/**
 * A  Data-Transfer-Object which contains all needed information for MediathekView.
 */
public class VideoDTO
{
    private String topic;

    public String getTopic() {
        return topic;
    }
    
    public void setTopic(String aTopic) {
        topic = aTopic;
    }
}
