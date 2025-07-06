package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.Objects;

public class ZdfPubFormDto extends CrawlerUrlDTO {
  private final String topic;
  private final String collectionId;

  public ZdfPubFormDto(String topic,String collectionId, String url) {
    super(url);
    this.topic = topic;
    this.collectionId = collectionId;
  }

  public String getCollectionId() {
    return collectionId;
  }


  public String getTopic() {
    return topic;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ZdfPubFormDto that = (ZdfPubFormDto) o;
    return Objects.equals(topic, that.topic) && Objects.equals(collectionId, that.collectionId);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(topic);
    result = 31 * result + Objects.hashCode(collectionId);
    return result;
  }
}
