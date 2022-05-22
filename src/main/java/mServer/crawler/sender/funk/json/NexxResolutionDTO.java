package mServer.crawler.sender.funk.json;

import java.util.Objects;
import java.util.Optional;

public class NexxResolutionDTO {
  private final int widht;
  private final int height;
  private final String size;
  private Optional<String> fileId;

  public NexxResolutionDTO(final int widht, final int height, final String size, Optional<String> aFileId) {
    this.widht = widht;
    this.height = height;
    this.size = size;
    this.fileId = aFileId;
  }

  public int getWidht() {
    return widht;
  }

  public int getHeight() {
    return height;
  }

  public String getSize() {
    return size;
  }

  public Optional<String> getFileId() {
    return fileId;
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
    return widht == that.widht && height == that.height && Objects.equals(size, that.size);
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
