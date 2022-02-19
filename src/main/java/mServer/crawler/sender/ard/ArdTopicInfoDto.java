package mServer.crawler.sender.ard;

import java.util.Objects;
import java.util.Set;

public class ArdTopicInfoDto {
  private final Set<ArdFilmInfoDto> filmInfos;
  private String id;
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

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
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
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    ArdTopicInfoDto that = (ArdTopicInfoDto) o;
    return Objects.equals(id, that.id) && subPageNumber == that.subPageNumber && maxSubPageNumber == that.maxSubPageNumber && Objects.equals(filmInfos, that.filmInfos);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, filmInfos, subPageNumber, maxSubPageNumber);
  }
}
