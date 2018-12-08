package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class ArdFilmInfoDto extends CrawlerUrlDTO {

  private String id;

  public ArdFilmInfoDto(String id, String aUrl) {
    super(aUrl);

    this.id = id;
  }

  public String getId() {
    return id;
  }
}
