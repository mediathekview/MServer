package de.mediathekview.mserver.crawler.artem;

import java.util.Optional;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class ArteMVideoDto extends CrawlerUrlDTO{

  Optional<String> id; 
  Optional<String> programId;
  Optional<String> language;
  Optional<String> kind;
  Optional<String> platform;
  Optional<String> platformLabel;
  Optional<String> title;
  Optional<String> subtitle;
  Optional<String> originalTitle;
  Optional<String> durationSeconds;
  Optional<String> shortDescription;
  Optional<String> headerText;
  Optional<String> geoblockingZone;
  Optional<String> website;
  Optional<String> videoStreams;
  Optional<String> creationDate;
  
  public ArteMVideoDto(ArteMVideoDto clone) {
    super(clone.getVideoStreams().orElse(""));
    this.id = clone.id;
    this.programId = clone.programId;
    this.language = clone.language;
    this.kind = clone.kind;
    this.platform = clone.platform;
    this.platformLabel = clone.platformLabel;
    this.title = clone.title;
    this.subtitle = clone.subtitle;
    this.originalTitle = clone.originalTitle;
    this.durationSeconds = clone.durationSeconds;
    this.shortDescription = clone.shortDescription;
    this.headerText = clone.headerText;
    this.geoblockingZone = clone.geoblockingZone;
    this.website = clone.website;
    this.videoStreams = clone.videoStreams;
    this.creationDate = clone.creationDate;
  }
  
  public ArteMVideoDto(Optional<String> id, Optional<String> programId, Optional<String> language,
      Optional<String> kind, Optional<String> platform, Optional<String> platformLabel, Optional<String> title,
      Optional<String> subtitle, Optional<String> originalTitle, Optional<String> durationSeconds,
      Optional<String> shortDescription, Optional<String> headerText, Optional<String> geoblockingZone,
      Optional<String> website, Optional<String> videoStreams, Optional<String> creationDate) {
    super(videoStreams.orElse(""));
    this.id = id;
    this.programId = programId;
    this.language = language;
    this.kind = kind;
    this.platform = platform;
    this.platformLabel = platformLabel;
    this.title = title;
    this.subtitle = subtitle;
    this.originalTitle = originalTitle;
    this.durationSeconds = durationSeconds;
    this.shortDescription = shortDescription;
    this.headerText = headerText;
    this.geoblockingZone = geoblockingZone;
    this.website = website;
    this.videoStreams = videoStreams;
    this.creationDate = creationDate;
  }
  public Optional<String> getId() {
    return id;
  }
  public Optional<String> getProgramId() {
    return programId;
  }
  public Optional<String> getLanguage() {
    return language;
  }
  public Optional<String> getKind() {
    return kind;
  }
  public Optional<String> getPlatform() {
    return platform;
  }
  public Optional<String> getPlatformLabel() {
    return platformLabel;
  }
  public Optional<String> getTitle() {
    return title;
  }
  public Optional<String> getSubtitle() {
    return subtitle;
  }
  public Optional<String> getOriginalTitle() {
    return originalTitle;
  }
  public Optional<String> getDurationSeconds() {
    return durationSeconds;
  }
  public Optional<String> getShortDescription() {
    return shortDescription;
  }
  public Optional<String> getHeaderText() {
    return headerText;
  }
  public Optional<String> getGeoblockingZone() {
    return geoblockingZone;
  }
  public Optional<String> getWebsite() {
    return website;
  }
  public Optional<String> getVideoStreams() {
    return videoStreams;
  }
  public Optional<String> getCreationDate() {
    return creationDate;
  }
  
  
}
