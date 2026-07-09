package mServer.crawler.sender.ard;

import java.util.Objects;
import java.util.Set;

public class ArdTopicInfoDto {
  private final Set<ArdFilmInfoDto> filmInfos;
  private String id;
  private int pageNumber;
  private int pageSize;
  private int totalElements;
  private int maxSubPageNumber;


  public ArdTopicInfoDto(final Set<ArdFilmInfoDto> filmInfos) {
    this.filmInfos = filmInfos;
    setPageNumber(0);
    setPageSize(0);
    setTotalElements(0);
    setMaxSubPageNumber(0);
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
    return getId() == that.getId()
            && getPageNumber() == that.getPageNumber()
            && getPageSize() == that.getPageSize()
            && getTotalElements() == that.getTotalElements()
            && Objects.equals(filmInfos, that.filmInfos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(filmInfos, getId(), getPageNumber(), getPageSize(), getTotalElements());
  }

  public String getId() { return id; }

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

  public void setId(String id) { this.id = id; }

  public int getMaxSubPageNumber() {
    return maxSubPageNumber;
  }

  public void setMaxSubPageNumber(final int maxSubPageNumber) {
    this.maxSubPageNumber = maxSubPageNumber;
  }
}
