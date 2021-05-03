package de.mediathekview.mserver.crawler.br;

import de.mediathekview.mserver.crawler.basic.GraphQlUrlDto;
import de.mediathekview.mserver.crawler.br.data.BrID;

import java.util.Objects;

public class BrClipQueryDto extends GraphQlUrlDto {
  private final BrID id;

  public BrClipQueryDto(String aUrl, BrID id) {
    super(aUrl, BrGraphQLQueries.getQuery2GetClipDetails(id));
    this.id = id;
  }

  public BrID getId() {
    return id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    BrClipQueryDto that = (BrClipQueryDto) o;
    return Objects.equals(id, that.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), id);
  }
}
