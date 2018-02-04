package de.mediathekview.mserver.crawler.basic;

import java.util.Objects;

public class TopicUrlDTO extends CrawlerUrlDTO {
  
  private final String theme;
  
  public TopicUrlDTO(String aTheme, String aUrl) {
    super(aUrl);
    theme = aTheme;
  }
  
  public String getTheme() {
    return theme;
  }
  
  @Override
  public boolean equals(final Object obj) {
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    if (super.equals(obj)) {
      return getTheme().equals(((TopicUrlDTO)obj).getTheme());
    }
    
    return false;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    hash = 31 * hash + Objects.hashCode(this.theme) + super.hashCode();
    return hash;
  }
}
