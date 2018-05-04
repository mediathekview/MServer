package mServer.crawler.sender.orf;

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
    if (super.equals(obj)) {
      return getTopic().equals(((TopicUrlDTO) obj).getTopic());
    }

    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.topic) + super.hashCode();
    return hash;
  }
}
