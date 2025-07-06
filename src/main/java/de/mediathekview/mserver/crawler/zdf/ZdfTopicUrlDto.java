package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;

public class ZdfTopicUrlDto extends TopicUrlDTO {
  private final int season;
  private final String canonical;

  public ZdfTopicUrlDto(String topic, int season, String canonical, String url) {
    super(topic, url);
    this.season = season;
    this.canonical = canonical;
  }

  public int getSeason() {
    return season;
  }

  public String getCanonical() {
    return canonical;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    ZdfTopicUrlDto that = (ZdfTopicUrlDto) o;
    return season == that.season && canonical.equals(that.canonical);
  }

  @Override
  public int hashCode() {
    int result = super.hashCode();
    result = 31 * result + season;
    result = 31 * result + canonical.hashCode();
    return result;
  }
}
