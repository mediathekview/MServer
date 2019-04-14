package de.mediathekview.mserver.crawler.funk;

import java.util.Objects;

/** Represents a Funk channel. */
public class FunkChannelDTO {
  private final String channelId;
  private final String channelTitle;

  /**
   * @param channelId The channel ID.
   * @param channelTitle The channel title.
   */
  public FunkChannelDTO(final String channelId, final String channelTitle) {
    this.channelId = channelId;
    this.channelTitle = channelTitle;
  }

  public String getChannelId() {
    return channelId;
  }

  public String getChannelTitle() {
    return channelTitle;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FunkChannelDTO that = (FunkChannelDTO) o;
    return getChannelId().equals(that.getChannelId())
        && getChannelTitle().equals(that.getChannelTitle());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getChannelId(), getChannelTitle());
  }

  @Override
  public String toString() {
    return "FunkChannelDTO{"
        + "channelId='"
        + channelId
        + '\''
        + ", channelTitle='"
        + channelTitle
        + '\''
        + '}';
  }
}
