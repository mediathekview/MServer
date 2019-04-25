package de.mediathekview.mserver.crawler.funk.json;

import java.util.Objects;

public class NexxResolutionDTO {
  private final int widht;
  private final int height;
  private final int size;

  public NexxResolutionDTO(final int widht, final int height, final int size) {
    this.widht = widht;
    this.height = height;
    this.size = size;
  }

  public int getWidht() {
    return widht;
  }

  public int getHeight() {
    return height;
  }

  public int getSize() {
    return size;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final NexxResolutionDTO that = (NexxResolutionDTO) o;
    return widht == that.widht && height == that.height && size == that.size;
  }

  @Override
  public int hashCode() {
    return Objects.hash(widht, height, size);
  }

  @Override
  public String toString() {
    return "NexxResolutionDTO{" + "widht=" + widht + ", height=" + height + ", size=" + size + '}';
  }
}
