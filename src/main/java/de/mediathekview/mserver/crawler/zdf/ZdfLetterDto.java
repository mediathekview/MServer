package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;

public class ZdfLetterDto extends CrawlerUrlDTO {
  private final int index;

  public ZdfLetterDto(int index, String aUrl) {
    super(aUrl);
    this.index = index;
  }

  public int getIndex() {
    return index;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ZdfLetterDto that = (ZdfLetterDto) o;
    return index == that.index;
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + index;
    return result;
  }
}
