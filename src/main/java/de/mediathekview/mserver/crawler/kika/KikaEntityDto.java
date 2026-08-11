package de.mediathekview.mserver.crawler.kika;

import java.util.Optional;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class KikaEntityDto extends CrawlerUrlDTO{
  
  Optional<String> docType; // "broadcastSeries"
  Optional<String> id; //  "die-abenteuer-von-awena-und-abduli-100"
  Optional<String> uuid; //  "4e5071e7-4c38-4c69-84b4-3c9c3e8c49b1"
  Optional<String> externalId; //  "content-63109"
  Optional<String> urlPath; // "/die-abenteuer-von-awena-und-abduli"
  Optional<String> apiId; //  "die-abenteuer-von-awena-und-abduli-100"
  //Optional<String> url; // "https://www.kika.de/ackley/v1/brands/die-abenteuer-von-awena-und-abduli-100"
  Optional<String> modificationDate; //modificationDate  "2026-05-04T21:02:41.605+02:00"
  
  public KikaEntityDto(Optional<String> docType, Optional<String> id, Optional<String> uuid,
      Optional<String> externalId, Optional<String> urlPath, Optional<String> apiId, Optional<String> url,
      Optional<String> modificationDate) {
    super(url.get());
    this.docType = docType;
    this.id = id;
    this.uuid = uuid;
    this.externalId = externalId;
    this.urlPath = urlPath;
    this.apiId = apiId;
    this.modificationDate = modificationDate;
  }

  public Optional<String> getDocType() {
    return docType;
  }

  public Optional<String> getId() {
    return id;
  }

  public Optional<String> getUuid() {
    return uuid;
  }

  public Optional<String> getExternalId() {
    return externalId;
  }

  public Optional<String> getUrlPath() {
    return urlPath;
  }

  public Optional<String> getApiId() {
    return apiId;
  }

  public Optional<String> getModificationDate() {
    return modificationDate;
  }

  @Override
  public boolean equals(Object o) {
      if (this == o) return true;
      if (!(o instanceof KikaEntityDto)) return false;
      KikaEntityDto other = (KikaEntityDto) o;
      return getUrl().equals(other.getUrl());
  }

  @Override
  public int hashCode() {
      return getUrl().hashCode();
  }
  

}
