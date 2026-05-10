package mServer.crawler.sender.ard;

import java.util.Objects;
import mServer.crawler.sender.base.CrawlerUrlDTO;

public class ArdFilmInfoDto extends CrawlerUrlDTO {

  private final String id;
  private final int numberOfClips;
  private final boolean isCompilation;

  public ArdFilmInfoDto(String id, String aUrl, int numberOfClips, boolean isCompilation) {
    super(aUrl);

    this.id = id;
    this.numberOfClips = numberOfClips;
    this.isCompilation = isCompilation;
  }

  public String getId() {
    return id;
  }

  public boolean isCompilation() {
    return isCompilation;
  }

  public int getNumberOfClips() {
    return numberOfClips;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArdFilmInfoDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    ArdFilmInfoDto that = (ArdFilmInfoDto) o;
    return numberOfClips == that.numberOfClips
            && Objects.equals(id, that.id)
            && isCompilation == that.isCompilation;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id, numberOfClips, isCompilation);
  }
}
