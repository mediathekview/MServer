package de.mediathekview.mserver.crawler.wdr;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Optional;

public class WdrMediaDTO extends CrawlerUrlDTO {
  
  private Optional<String> subtitle;
  
  public WdrMediaDTO(String aUrl) {
    super(aUrl);
    subtitle = Optional.empty();
  }

  public Optional<String> getSubtitle() {
    return subtitle;
  }
  
  public void setSubtitle(final String aSubtitle) {
    subtitle = Optional.of(aSubtitle);
  }
}
