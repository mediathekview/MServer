package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Objects;

public class ArdFilmInfoDto extends CrawlerUrlDTO {

  private final String id;
  private final int numberOfClips;

  public ArdFilmInfoDto(String id, String aUrl, int numberOfClips) {
    super(aUrl);

    this.id = id;
    this.numberOfClips = numberOfClips;
  }

  public String getId() {
    return id;
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
        && Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id, numberOfClips);
  }
}
