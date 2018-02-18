package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.Objects;

public class WdrTopicUrlDTO extends TopicUrlDTO {
  
  private final boolean isFileUrl; 
  
  public WdrTopicUrlDTO(final String aTopic, final String aUrl, final boolean aIsFileUrl) {
    super(aTopic, aUrl);
    
    isFileUrl = aIsFileUrl;
  }
  
  public boolean isFileUrl() {
    return isFileUrl;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    if (super.equals(obj)) {
      return isFileUrl() == ((WdrTopicUrlDTO)obj).isFileUrl();
    }
    
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.isFileUrl) + super.hashCode();
    return hash;
  }  
}
