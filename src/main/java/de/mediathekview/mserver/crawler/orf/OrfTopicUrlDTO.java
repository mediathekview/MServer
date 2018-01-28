package de.mediathekview.mserver.crawler.orf;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class OrfTopicUrlDTO extends CrawlerUrlDTO {
  
  private final String theme;
  
  public OrfTopicUrlDTO(String aTheme, String aUrl) {
    super(aUrl);
    theme = aTheme;
  }
  
  public String getTheme() {
    return theme;
  }
}
