package de.mediathekview.mserver.crawler.arte;

import java.net.URL;
import java.util.Optional;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class ArteCrawlerUrlDto extends CrawlerUrlDTO {
  private Optional<String> category;

  public ArteCrawlerUrlDto(final String aUrl) {
    super(aUrl);
  }

  public ArteCrawlerUrlDto(final URL aUrl) {
    super(aUrl);
  }

  public Optional<String> getCategory() {
    return category;
  }

  public void setCategory(final Optional<String> aCategory) {
    category = aCategory;
  }

}
