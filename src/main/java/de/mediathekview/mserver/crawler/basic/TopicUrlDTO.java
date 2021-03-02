package de.mediathekview.mserver.crawler.basic;

import java.util.Objects;

public class TopicUrlDTO extends CrawlerUrlDTO {
  
  private final String topic;
  
  public TopicUrlDTO(String aTopic, String aUrl) {
    super(aUrl);
    topic = aTopic;
  }
  
  public String getTopic() {
    return topic;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    // don't compare topic because different topics with the same url should be treated as equal
    return super.equals(obj);
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.topic) + super.hashCode();
    return hash;
  }
}
