package mServer.crawler.sender.sr;

import mServer.crawler.sender.base.CrawlerUrlDTO;

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
