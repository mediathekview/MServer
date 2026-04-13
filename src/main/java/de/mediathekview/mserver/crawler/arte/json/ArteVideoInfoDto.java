package de.mediathekview.mserver.crawler.arte.json;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import de.mediathekview.mserver.crawler.arte.ArteConstants;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.daten.Sender;

public class ArteVideoInfoDto extends CrawlerUrlDTO {
  private Optional<String> firstBroadcastDate;
  private Optional<String> id;
  private Optional<String> programId;
  private Optional<String> channel;
  private Optional<String> language;
  private Optional<String> kind;
  private Optional<String> catalogType;
  private Optional<String> programType;
  private Optional<String> platform;
  private Optional<String> platformLabel;
  private Optional<String> title;
  private Optional<String> subtitle;
  private Optional<String> originalTitle;
  private Optional<String> durationSeconds;
  private Optional<String> shortDescription;
  private Optional<String> fullDescription;
  private Optional<String> headerText;
  private Optional<String> geoblockingZone;
  private Optional<String> website;
  private Optional<String> season;
  private Optional<String> episode;
  private Optional<String> broadcastBegin;
  private Optional<String> broadcastBeginRounded;
  private Optional<String> category;
  private Optional<String> categoryName;
  private Optional<String> subcategoryName;
  private Optional<String> creationDate;
  private Optional<String> pageIndex;
  private List<ArteVideoLinkDto> videoLinks;
  private List<ArteSubtitleLinkDto> subtitleLinks;
  
  // ONLY for unit tests
  public ArteVideoInfoDto(Optional<String> id, Optional<String> programId,Optional<String> kind, Optional<String> language) {
    super(String.format(ArteConstants.VIDEO_URL, programId.get(), kind.get(), language.get()));
    this.id = id;
  }
  
  public ArteVideoInfoDto(Optional<String> firstBroadcastDate, Optional<String> id, Optional<String> programId, Optional<String> channel, Optional<String> language,
      Optional<String> kind, Optional<String> catalogType, Optional<String> programType, Optional<String> platform, Optional<String> platformLabel, Optional<String> title,
      Optional<String> subtitle, Optional<String> originalTitle, Optional<String> durationSeconds, Optional<String> shortDescription, Optional<String> fullDescription,
      Optional<String> headerText, Optional<String> geoblockingZone, Optional<String> url, Optional<String> season, Optional<String> episode, Optional<String> broadcastBegin,
      Optional<String> broadcastBeginRounded,Optional<String> category, Optional<String> categoryName, Optional<String> subcategoryName, Optional<String> creationDate, Optional<String> pageIndex) {
    super(String.format(ArteConstants.VIDEO_URL, programId.orElseThrow(), kind.orElseThrow(), language.orElseThrow()));
    this.firstBroadcastDate = firstBroadcastDate;
    this.id = id;
    this.programId = programId;
    this.channel = channel;
    this.language = language;
    this.kind = kind;
    this.catalogType = catalogType;
    this.programType = programType;
    this.platform = platform;
    this.platformLabel = platformLabel;
    this.title = title;
    this.subtitle = subtitle;
    this.originalTitle = originalTitle;
    this.durationSeconds = durationSeconds;
    this.shortDescription = shortDescription;
    this.fullDescription = fullDescription;
    this.headerText = headerText;
    this.geoblockingZone = geoblockingZone;
    this.website = url;
    this.season = season;
    this.episode = episode;
    this.broadcastBegin = broadcastBegin;
    this.broadcastBeginRounded = broadcastBeginRounded;
    this.category = category;
    this.categoryName = categoryName;
    this.subcategoryName = subcategoryName;
    this.creationDate = creationDate;
    this.pageIndex = pageIndex;
  }
  
  public Optional<String> getFirstBroadcastDate() {
    return firstBroadcastDate;
  }
  public String getId() {
    return id.orElse("");
  }
  public Optional<String> getProgramId() {
    return programId;
  }
  public Optional<String> getChannel() {
    return channel;
  }
  public Optional<String> getLanguage() {
    return language;
  }
  public Optional<String> getKind() {
    return kind;
  }
  public Optional<String> getCatalogType() {
    return catalogType;
  }
  public Optional<String> getProgramType() {
    return programType;
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
  public Optional<String> getFullDescription() {
    return fullDescription;
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
  public Optional<String> getSeason() {
    return season;
  }
  public Optional<String> getEpisode() {
    return episode;
  }
  public Optional<String> getBroadcastBegin() {
    return broadcastBegin;
  }
  public Optional<String> getBroadcastBeginRounded() {
    return broadcastBeginRounded;
  }
  public Optional<String> getCategory() {
    return category;
  }
  public Optional<String> getCategoryName() {
    return categoryName;
  }
  public Optional<String> getSubcategoryName() {
    return subcategoryName;
  }
  public Optional<String> getCreationDate() {
    return creationDate;
  }
  public Optional<String> getPageIndex() {
    return pageIndex;
  }
  
  public List<ArteVideoLinkDto> getVideoLinks() {
    return videoLinks;
  }
  public void setVideoLinks(List<ArteVideoLinkDto> input) {
    videoLinks = input;
  }
  
  public List<ArteSubtitleLinkDto> getSubtitleLinks() {
    return subtitleLinks;
  }
  public void setSubtitleLinks(List<ArteSubtitleLinkDto> subtitleLinks) {
    this.subtitleLinks = subtitleLinks;
  }
  
  public Sender getSender() {
    return switch (getLanguage().get()) {
        case "de" -> Sender.ARTE_DE;
        case "en" -> Sender.ARTE_EN;
        case "fr" -> Sender.ARTE_FR;
        case "es" -> Sender.ARTE_ES;
        case "it" -> Sender.ARTE_IT;
        case "pl" -> Sender.ARTE_PL;
        default -> Sender.ARTE_DE;
    };
}
    
  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass())
        return false;
      ArteVideoInfoDto that = (ArteVideoInfoDto) o;
      return Objects.equals(this.id.get(), that.id.get());
  }

  @Override
  public int hashCode() {
      return Objects.hash(id.get());
  }
  
  
}
