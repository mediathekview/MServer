package de.mediathekview.mserver.crawler.kika.json;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaApiFilmDto extends CrawlerUrlDTO {
  private Optional<String> errorMesssage = Optional.empty();
  private Optional<String> errorCode = Optional.empty();
  private Optional<String> topic = Optional.empty();
  private Optional<String> title = Optional.empty();
  private Optional<String> id = Optional.empty();
  private Optional<String> description = Optional.empty();
  private Optional<LocalDateTime> date = Optional.empty();
  private Optional<Duration> duration = Optional.empty();
  private Optional<GeoLocations> geoProtection = Optional.empty();
  private Optional<String> website = Optional.empty();
  //
  private Optional<LocalDateTime> expirationDate = Optional.empty();
  private Optional<LocalDateTime> appearDate = Optional.empty();

  public KikaApiFilmDto(String aUrl, Optional<String> topic, Optional<String> title, Optional<String> id,
      Optional<String> description, Optional<LocalDateTime> date, Optional<Duration> duration,
      Optional<GeoLocations> geoProtection, Optional<LocalDateTime> expirationDate, Optional<LocalDateTime> appearDate,
      Optional<String> website) {
    super(aUrl);
    this.topic = topic;
    this.title = title;
    this.id = id;
    this.expirationDate = expirationDate;
    this.appearDate = appearDate;
    this.description = description;
    this.date = date;
    this.duration = duration;
    this.geoProtection = geoProtection;
    this.website = website;
  }

  public void setTopic(Optional<String> aTopic) {
    topic = aTopic;
  }
  public Optional<String> getTopic() {
    return topic;
  }

  public Optional<String> getTitle() {
    return title;
  }

  public Optional<String> getId() {
    return id;
  }

  public Optional<String> getDescription() {
    return description;
  }

  public Optional<LocalDateTime> getDate() {
    return date;
  }

  public Optional<Duration> getDuration() {
    return duration;
  }

  public Optional<GeoLocations> getGeoProtection() {
    return geoProtection;
  }

  public Optional<LocalDateTime> getExpirationDate() {
    return expirationDate;
  }

  public Optional<LocalDateTime> getAppearDate() {
    return appearDate;
  }

  public Optional<String> getWebsite() {
    return website;
  }

  public void setError(Optional<String> aErrorCode, Optional<String> aErrorMesssage) {
    errorCode = aErrorCode;
    errorMesssage = aErrorMesssage;
  }
  
  public Optional<String> getErrorMesssage() {
    return errorMesssage;
  }

  public Optional<String> getErrorCode() {
    return errorCode;
  }
}
