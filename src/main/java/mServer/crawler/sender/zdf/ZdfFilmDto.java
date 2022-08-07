package mServer.crawler.sender.zdf;

import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Optional;

public class ZdfFilmDto extends CrawlerUrlDTO {

  private final Optional<String> topic;
  private final String title;
  private final Optional<String> description;
  private final Optional<String> website;
  private final Optional<LocalDateTime> time;
  private final Optional<Duration> duration;
  private final Optional<String> urlSignLanguage;

  public ZdfFilmDto(String url, Optional<String> topic, String title,
                    Optional<String> description, Optional<String> website,
                    Optional<LocalDateTime> time, Optional<Duration> duration, String urlSignLanguage) {
    super(url);
    this.topic = topic;
    this.title = title;
    this.description = description;
    this.website = website;
    this.time = time;
    this.duration = duration;

    if (urlSignLanguage != null && !urlSignLanguage.isEmpty()) {
      this.urlSignLanguage = Optional.of(urlSignLanguage);
    } else {
      this.urlSignLanguage = Optional.empty();
    }
  }

  public Optional<String> getTopic() {
    return topic;
  }

  public String getTitle() {
    return title;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public Optional<String> getWebsite() {
    return website;
  }

  public Optional<LocalDateTime> getTime() {
    return time;
  }

  public Optional<Duration> getDuration() {
    return duration;
  }

  public Optional<String> getUrlSignLanguage() {
    return urlSignLanguage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZdfFilmDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ZdfFilmDto that = (ZdfFilmDto) o;
    return Objects.equals(topic, that.topic)
            && Objects.equals(title, that.title)
            && Objects.equals(description, that.description)
            && Objects.equals(website, that.website)
            && Objects.equals(time, that.time)
            && Objects.equals(duration, that.duration)
            && Objects.equals(urlSignLanguage, that.urlSignLanguage);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), topic, title, description, website, time, duration, urlSignLanguage);
  }
}
