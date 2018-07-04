package de.mediathekview.mserver.crawler.phoenix.parser;

import java.time.Duration;
import java.time.LocalDateTime;

public class PhoenixFilmXmlDto {

  private String baseName;
  private LocalDateTime time;
  private Duration duration;

  public String getBaseName() {
    return baseName;
  }

  public void setBaseName(String baseName) {
    this.baseName = baseName;
  }

  public LocalDateTime getTime() {
    return time;
  }

  public void setTime(LocalDateTime time) {
    this.time = time;
  }

  public Duration getDuration() {
    return duration;
  }

  public void setDuration(Duration duration) {
    this.duration = duration;
  }
}
