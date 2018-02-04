package de.mediathekview.mserver.crawler.orf;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Objects;

public class OrfTopicUrlDTO extends CrawlerUrlDTO {
  
  private final String theme;
  
  public OrfTopicUrlDTO(String aTheme, String aUrl) {
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
      return getTheme().equals(((OrfTopicUrlDTO)obj).getTheme());
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
