package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.time.LocalDateTime;
import java.util.Objects;

public class ZdfFilmDto extends CrawlerUrlDTO {
  private final Sender sender;
  private final String title;
  private final String description;
  private final String website;
  private final LocalDateTime time;
  private final String videoType;
  private String topic;

  public ZdfFilmDto(
          Sender sender,
          String title,
          String description,
          String website,
          LocalDateTime time,
          String videoType,
          String downloadUrl) {
    super(downloadUrl);
    this.topic = "";
    this.title = title;
    this.description = description;
    this.sender = sender;
    this.website = website;
    this.time = time;
    this.videoType = videoType;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public Sender getSender() {
    return sender;
  }

  public String getWebsite() {
    return website;
  }

  public LocalDateTime getTime() {
    return time;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ZdfFilmDto filmDto = (ZdfFilmDto) o;
    return sender == filmDto.sender && Objects.equals(topic, filmDto.topic) && Objects.equals(title, filmDto.title) && Objects.equals(description, filmDto.description) && Objects.equals(website, filmDto.website) && Objects.equals(time, filmDto.time) && Objects.equals(videoType, filmDto.videoType);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(sender);
    result = 31 * result + Objects.hashCode(topic);
    result = 31 * result + Objects.hashCode(title);
    result = 31 * result + Objects.hashCode(description);
    result = 31 * result + Objects.hashCode(website);
    result = 31 * result + Objects.hashCode(time);
    result = 31 * result + Objects.hashCode(videoType);
    return result;
  }

  public String getVideoType() {
    return videoType;
  }

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }
}
