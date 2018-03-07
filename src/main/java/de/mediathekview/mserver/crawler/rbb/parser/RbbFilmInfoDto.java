package de.mediathekview.mserver.crawler.rbb.parser;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.time.Duration;
import java.time.LocalDateTime;

public class RbbFilmInfoDto extends CrawlerUrlDTO {

  private String topic;
  private String title;
  private LocalDateTime time;
  private Duration duration;
  private String description;
  private String website;

  public RbbFilmInfoDto(String aUrl) {
    super(aUrl);
  }

  public String getDescription() {
    return description;
  }

  public Duration getDuration() {
    return duration;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public String getTitle() {
    return title;
  }

  public String getTopic() {
    return topic;
  }

  public String getWebsite() {
    return website;
  }

  public void setDescription(String aDescription) {
    this.description = aDescription;
  }

  public void setDuration(Duration aDuration) {
    this.duration = aDuration;
  }

  public void setTime(LocalDateTime aTime) {
    this.time = aTime;
  }

  public void setTitle(String aTitle) {
    this.title = aTitle;
  }

  public void setTopic(String aTopic) {
    this.topic = aTopic;
  }

  public void setWebsite(String aWebsite) {
    this.website = aWebsite;
  }
}
