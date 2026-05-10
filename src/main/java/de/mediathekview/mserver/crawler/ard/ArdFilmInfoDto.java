package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

import java.util.Objects;

public class ArdFilmInfoDto extends CrawlerUrlDTO {

  private final String id;
  private final int numberOfClips;
  private final boolean isCompilation;

  public ArdFilmInfoDto(final String id, final String aUrl, final int numberOfClips, final boolean isCompilation) {
    super(aUrl);

    this.id = id;
    this.numberOfClips = numberOfClips;
    this.isCompilation = isCompilation;
  }

  public String getId() {
    return id;
  }

  public int getNumberOfClips() {
    return numberOfClips;
  }

  public boolean isCompilation() { return isCompilation; }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof final ArdFilmInfoDto that)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    return numberOfClips == that.numberOfClips && Objects.equals(id, that.id) && isCompilation == that.isCompilation;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id, numberOfClips, isCompilation);
  }
}
