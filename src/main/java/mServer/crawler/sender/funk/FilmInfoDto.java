package mServer.crawler.sender.funk;

import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class FilmInfoDto extends CrawlerUrlDTO {

  private String topic;
  private String title;
  private LocalDateTime time;
  private Duration duration;
  private String description;
  private String website;

  public FilmInfoDto(final String aUrl) {
    super(aUrl);
    topic = "";
    title = "";
  }

  public Optional<String> getDescription() {
    return Optional.ofNullable(description);
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

  public Optional<String> getWebsite() {
    return Optional.ofNullable(website);
  }

  public void setDescription(final String aDescription) {
    description = aDescription;
  }

  public void setDuration(final Duration aDuration) {
    duration = aDuration;
  }

  public void setTime(final LocalDateTime aTime) {
    time = aTime;
  }

  public void setTitle(final String aTitle) {
    title = aTitle;
  }

  public void setTopic(final String aTopic) {
    topic = aTopic;
  }

  public void setWebsite(final String aWebsite) {
    website = aWebsite;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    final FilmInfoDto that = (FilmInfoDto) o;
    return Objects.equals(topic, that.topic)
            && Objects.equals(title, that.title)
            && Objects.equals(time, that.time)
            && Objects.equals(duration, that.duration)
            && Objects.equals(description, that.description)
            && Objects.equals(website, that.website);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), topic, title, time, duration, description, website);
  }

  @Override
  public String toString() {
    return "FilmInfoDto{"
            + "topic='"
            + topic
            + '\''
            + ", title='"
            + title
            + '\''
            + ", time="
            + time
            + ", duration="
            + duration
            + ", description='"
            + description
            + '\''
            + ", website='"
            + website
            + '\''
            + '}';
  }
}
