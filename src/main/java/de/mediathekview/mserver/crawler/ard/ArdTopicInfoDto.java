package de.mediathekview.mserver.crawler.ard;

import java.util.Objects;
import java.util.Set;

public class ArdTopicInfoDto {
  private final Set<ArdFilmInfoDto> filmInfos;
  private int subPageNumber;
  private int maxSubPageNumber;

  public ArdTopicInfoDto(final Set<ArdFilmInfoDto> filmInfos) {
    this.filmInfos = filmInfos;
    subPageNumber = 0;
    maxSubPageNumber = 0;
  }

  public Set<ArdFilmInfoDto> getFilmInfos() {
    return filmInfos;
  }

  public int getSubPageNumber() {
    return subPageNumber;
  }

  public void setSubPageNumber(final int subPageNumber) {
    this.subPageNumber = subPageNumber;
  }

  public int getMaxSubPageNumber() {
    return maxSubPageNumber;
  }

  public void setMaxSubPageNumber(final int maxSubPageNumber) {
    this.maxSubPageNumber = maxSubPageNumber;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ArdTopicInfoDto)) {
      return false;
    }
    final ArdTopicInfoDto that = (ArdTopicInfoDto) o;
    return getSubPageNumber() == that.getSubPageNumber()
        && getMaxSubPageNumber() == that.getMaxSubPageNumber()
        && Objects.equals(filmInfos, that.filmInfos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filmInfos, getSubPageNumber(), getMaxSubPageNumber());
  }
}
