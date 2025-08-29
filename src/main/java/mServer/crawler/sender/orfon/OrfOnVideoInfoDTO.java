package mServer.crawler.sender.orfon;

import mServer.crawler.sender.base.Qualities;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

public class OrfOnVideoInfoDTO {
  private Optional<String> id;
  private Optional<String> channel;
  private Optional<String> title;
  private Optional<String> titleWithDate;
  private Optional<String> topic;
  private Optional<String> topicForArchive;
  private Optional<String> drmProtected;
  private Optional<LocalDateTime> aired;
  private Optional<Duration> duration;
  private Optional<String> description;
  private Optional<String> website;
  private Optional<Map<Qualities, String>> videoUrls;
  private Optional<String> subtitleUrl;
  
  public OrfOnVideoInfoDTO(
      Optional<String> id, 
      Optional<String> channel, 
      Optional<String> title, 
      Optional<String> titleWithDate, 
      Optional<String> topic,
      Optional<String> topicForArchive,
      Optional<String> drmProtected,
      Optional<LocalDateTime> aired, 
      Optional<Duration> duration, 
      Optional<String> description, 
      Optional<String> website,
      Optional<Map<Qualities, String>> videoUrls,
      Optional<String> subtitleUrl) {
    super();
    this.id = id;
    this.channel = channel;
    this.title = title;
    this.titleWithDate = titleWithDate;
    this.topic = topic;
    this.topicForArchive = topicForArchive;
    this.drmProtected = drmProtected;
    this.aired = aired;
    this.duration = duration;
    this.description = description;
    this.website = website;
    this.videoUrls = videoUrls;
    this.subtitleUrl = subtitleUrl;
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
  public Optional<String> getTopicForArchive() {
    return topicForArchive;
  }
  public Optional<String> getDrmProtected() {
    return drmProtected;
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
  public Optional<String> getWebsite() {
    return website;
  }
  public Optional<Map<Qualities, String>> getVideoUrls() {
    return videoUrls;
  }
  public Optional<String> getSubtitleUrl() {
    return subtitleUrl;
  }

  @Override
  public int hashCode() {
    if (getId().isPresent()) {
      return Integer.valueOf(getId().get());
    }
    return super.hashCode();
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
   return this.hashCode() == obj.hashCode();
  }
}
