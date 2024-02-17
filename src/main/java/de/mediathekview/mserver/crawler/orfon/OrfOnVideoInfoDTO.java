package de.mediathekview.mserver.crawler.orfon;

import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;


public class OrfOnVideoInfoDTO {
  private Optional<String> id;
  private Optional<String> channel;
  private Optional<String> title;
  private Optional<String> titleWithDate;
  private Optional<String> topic;
  private Optional<LocalDateTime> aired;
  private Optional<Duration> duration;
  private Optional<String> description;
  private Optional<URL> website;
  private Optional<Collection<GeoLocations>> georestriction;
  private Optional<URL> subtitleSource;
  private Optional<Map<Resolution, FilmUrl>> videoUrls;
  
  public OrfOnVideoInfoDTO(
      Optional<String> id, 
      Optional<String> channel, 
      Optional<String> title, 
      Optional<String> titleWithDate, 
      Optional<String> topic,
      Optional<LocalDateTime> aired, 
      Optional<Duration> duration, 
      Optional<String> description, 
      Optional<URL> website,
      Optional<Collection<GeoLocations>> georestriction, 
      Optional<URL> subtitleSource,
      Optional<Map<Resolution, FilmUrl>> videoUrls) {
    super();
    this.id = id;
    this.channel = channel;
    this.title = title;
    this.titleWithDate = titleWithDate;
    this.topic = topic;
    this.aired = aired;
    this.duration = duration;
    this.description = description;
    this.website = website;
    this.georestriction = georestriction;
    this.subtitleSource = subtitleSource;
    this.videoUrls = videoUrls;
  }
  
  public Optional<String> getId() {
    return id;
  }
  public Optional<String> getChannel() {
    return channel;
  }
  public Optional<String> getTitle() {
    return title;
  }
  public Optional<String> getTitleWithDate() {
    return titleWithDate;
  }
  public Optional<String> getTopic() {
    return topic;
  }
  public void setTopic(Optional<String> newTopic) {
    topic = newTopic;
  }
  public Optional<LocalDateTime> getAired() {
    return aired;
  }
  public Optional<Duration> getDuration() {
    return duration;
  }
  public Optional<String> getDescription() {
    return description;
  }
  public Optional<URL> getWebsite() {
    return website;
  }
  public Optional<Collection<GeoLocations>> getGeorestriction() {
    return georestriction;
  }
  public Optional<URL> getSubtitleSource() {
    return subtitleSource;
  }
  public Optional<Map<Resolution, FilmUrl>> getVideoUrls() {
    return videoUrls;
  }

  @Override
  public int hashCode() {
    if (getId().isPresent()) {
      return Integer.valueOf(getId().get());
    }
    return super.hashCode();
  }
  
  
}
