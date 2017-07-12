package mServer.crawler.sender.arte;
import java.time.LocalDateTime;

public class ArteVideoDetailsDTO {
    private LocalDateTime broadcastBegin;;
    
    public LocalDateTime getBroadcastBegin() {
        return broadcastBegin;
    }
    
    public void setBroadcastBegin(LocalDateTime aBroadcastBegin) {
        broadcastBegin = aBroadcastBegin;
    }
}
