package mServer.crawler.sender.arte;

import mServer.crawler.sender.base.GeoLocations;

import java.time.Duration;
import java.time.LocalDateTime;

public class ArteFilmDto {
  private final String sender;
  private final String title;
  private final String topic;
  private final LocalDateTime date;
  private Duration duration;
  private final String description;
  private final String website;
  private GeoLocations geoLocations = GeoLocations.GEO_NONE;

  public ArteFilmDto(String sender, String topic, String title, LocalDateTime date, Duration duration, String description, String website) {

    this.sender = sender;
    this.title = title;
    this.topic = topic;
    this.date = date;
    this.duration = duration;
    this.description = description;
    this.website = website;
  }

  public void addGeolocation(GeoLocations geoLocation) {
    this.geoLocations = geoLocation;
  }

  public String getSender() {
    return sender;
  }

  public String getTitle() {
    return title;
  }

  public String getTopic() {
    return topic;
  }

  public LocalDateTime getDate() {
    return date;
  }

  public Duration getDuration() {
    return duration;
  }

  public String getDescription() {
    return description;
  }

  public String getWebsite() {
    return website;
  }

  public GeoLocations getGeoLocations() {
    return geoLocations;
  }

  public void setDuration(Duration duration) { this.duration = duration; }
}
