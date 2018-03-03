package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

/**
 * DTO containing the urls to determine the infos of a film
 */
public class ZdfEntryDto extends CrawlerUrlDTO {

  private final String videoUrl;

  public ZdfEntryDto(final String aDetailUrl, final String aVideoUrl) {
    super(aDetailUrl);
    videoUrl = aVideoUrl;
  }

  public String getVideoUrl() {
    return videoUrl;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ZdfEntryDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    ZdfEntryDto that = (ZdfEntryDto) o;

    return videoUrl != null ? videoUrl.equals(that.videoUrl) : that.videoUrl == null;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + (videoUrl != null ? videoUrl.hashCode() : 0);
    return result;
  }
}
