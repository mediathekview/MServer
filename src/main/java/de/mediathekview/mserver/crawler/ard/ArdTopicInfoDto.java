package de.mediathekview.mserver.crawler.ard;

import java.util.Objects;
import java.util.Set;

public class ArdTopicInfoDto {
  private final Set<ArdFilmInfoDto> filmInfos;
  private int pageNumber;
  private int pageSize;
  private int totalElements;
  

  public ArdTopicInfoDto(final Set<ArdFilmInfoDto> filmInfos) {
    this.filmInfos = filmInfos;
    setPageNumber(0);
    setPageSize(0);
    setTotalElements(0);
  }

  public Set<ArdFilmInfoDto> getFilmInfos() {
    return filmInfos;
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof final ArdTopicInfoDto that)) {
      return false;
    }
    return getPageNumber() == that.getPageNumber()
        && getPageSize() == that.getPageSize()
        && getTotalElements() == that.getTotalElements()
        && Objects.equals(filmInfos, that.filmInfos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filmInfos, getPageNumber(), getPageSize(), getTotalElements());
  }

  public int getPageNumber() {
    return pageNumber;
  }

  public void setPageNumber(int pageNumber) {
    this.pageNumber = pageNumber;
  }

  public int getPageSize() {
    return pageSize;
  }

  public void setPageSize(int pageSize) {
    this.pageSize = pageSize;
  }

  public int getTotalElements() {
    return totalElements;
  }

  public void setTotalElements(int totalElements) {
    this.totalElements = totalElements;
  }
}
