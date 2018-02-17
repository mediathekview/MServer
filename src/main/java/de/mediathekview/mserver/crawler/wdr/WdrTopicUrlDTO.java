package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

public class WdrTopicUrlDTO extends TopicUrlDTO {
  
  private final boolean isFileUrl; 
  
  public WdrTopicUrlDTO(final String aTheme, final String aUrl, final boolean aIsFileUrl) {
    super(aTheme, aUrl);
    
    isFileUrl = aIsFileUrl;
  }
  
  public boolean isFileUrl() {
    return isFileUrl;
  }
}
