package mServer.crawler.sender.arte;

import mServer.crawler.sender.base.CrawlerUrlDTO;

import java.util.Objects;

public class ArteFilmUrlDto extends CrawlerUrlDTO {

  private final String videoDetailsUrl;

  public ArteFilmUrlDto(String aFilmDetailsUrl, String aVideoDetailsUrl) {
    super(aFilmDetailsUrl);
    this.videoDetailsUrl = aVideoDetailsUrl;
  }

  public CrawlerUrlDTO getVideoDetailsUrl() {
    return new CrawlerUrlDTO(videoDetailsUrl);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArteFilmUrlDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ArteFilmUrlDto that = (ArteFilmUrlDto) o;
    return Objects.equals(videoDetailsUrl, that.videoDetailsUrl);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), videoDetailsUrl);
  }
}
