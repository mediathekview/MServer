package de.mediathekview.mserver.crawler.br;

import de.mediathekview.mserver.crawler.basic.GraphQlUrlDto;
import java.time.LocalDate;
import java.util.Objects;
import java.util.Optional;

public class BrQueryDto extends GraphQlUrlDto {

  private final LocalDate start;
  private final LocalDate end;
  private final int pageSize;
  private final Optional<String> cursor;

  public BrQueryDto(
      String url, LocalDate start, LocalDate end, int pageSize, Optional<String> cursor) {
    super(url, BrGraphQLQueries.getQueryGetIds(start, end, pageSize, cursor));
    this.start = start;
    this.end = end;
    this.pageSize = pageSize;
    this.cursor = cursor;
  }

  public LocalDate getStart() {
    return start;
  }

  public LocalDate getEnd() {
    return end;
  }

  public int getPageSize() {
    return pageSize;
  }

  public Optional<String> getCursor() {
    return cursor;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof BrQueryDto)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    BrQueryDto that = (BrQueryDto) o;
    return pageSize == that.pageSize
        && Objects.equals(start, that.start)
        && Objects.equals(end, that.end);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), start, end, pageSize);
  }
}
