package de.mediathekview.mserver.crawler.sr;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class SrTopicUrlDTO extends CrawlerUrlDTO {
  
  private final String theme;
  
  public SrTopicUrlDTO(String aTheme, String aUrl) {
    super(aUrl);
    theme = aTheme;
  }
  
  public String getTheme() {
    return theme;
  }
}
