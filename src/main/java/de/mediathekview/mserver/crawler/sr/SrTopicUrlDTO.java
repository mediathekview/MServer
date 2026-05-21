package de.mediathekview.mserver.crawler.sr;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.Objects;

public class SrTopicUrlDTO extends CrawlerUrlDTO {
  
  private final String theme;

  @Override
  public final boolean equals(Object o) {
    if (!(o instanceof SrTopicUrlDTO that)) return false;
    if (!super.equals(o)) return false;

    return Objects.equals(theme, that.theme);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + Objects.hashCode(theme);
    return result;
  }

  public SrTopicUrlDTO(String aTheme, String aUrl) {
    super(aUrl);
    theme = aTheme;
  }
  
  public String getTheme() {
    return theme;
  }
}
