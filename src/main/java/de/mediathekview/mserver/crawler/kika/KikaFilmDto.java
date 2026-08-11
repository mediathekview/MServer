package de.mediathekview.mserver.crawler.kika;

import java.util.List;
import java.util.Optional;

public class KikaFilmDto extends KikaEntityDto{
  Optional<String> date;
  Optional<String> title;
  Optional<String> teaserText;
  Optional<String> broadcastSeriesTitle;
  Optional<String> genDate;
  Optional<String> description;
  Optional<String> episodeNumber;
  Optional<String> durationInSeconds;
  Optional<String> season;
  List<KikaAssetDto> assets;
  public KikaFilmDto(Optional<String> docType, Optional<String> id, Optional<String> uuid, Optional<String> externalId,
      Optional<String> urlPath, Optional<String> apiId, Optional<String> url, Optional<String> modificationDate,
      Optional<String> date, Optional<String> title, Optional<String> teaserText, Optional<String> broadcastSeriesTitle,
      Optional<String> genDate, Optional<String> description, Optional<String> episodeNumber,
      Optional<String> durationInSeconds, Optional<String> season) {
    super(docType, id, uuid, externalId, urlPath, apiId, url, modificationDate);
    this.date = date;
    this.title = title;
    this.teaserText = teaserText;
    this.broadcastSeriesTitle = broadcastSeriesTitle;
    this.genDate = genDate;
    this.description = description;
    this.episodeNumber = episodeNumber;
    this.durationInSeconds = durationInSeconds;
    this.season = season;
  }
  public List<KikaAssetDto> getAssets() {
    return assets;
  }
  public void setAssets(List<KikaAssetDto> assets) {
    this.assets = assets;
  }
  public Optional<String> getDate() {
    return date;
  }
  public Optional<String> getTitle() {
    return title;
  }
  public Optional<String> getTeaserText() {
    return teaserText;
  }
  public Optional<String> getBroadcastSeriesTitle() {
    return broadcastSeriesTitle;
  }
  public Optional<String> getGenDate() {
    return genDate;
  }
  public Optional<String> getDescription() {
    return description;
  }
  public Optional<String> getEpisodeNumber() {
    return episodeNumber;
  }
  public Optional<String> getDurationInSeconds() {
    return durationInSeconds;
  }
  public Optional<String> getSeason() {
    return season;
  }
  
  
  
  

  
}
