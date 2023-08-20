package de.mediathekview.mserver.base.config;

import java.util.Objects;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;

public class ImportLivestreamConfiguration {
  private final Boolean active;
  private final String path;
  private final FilmlistFormats format;

  public ImportLivestreamConfiguration(Boolean active, String path, FilmlistFormats format) {
    super();
    this.active = active;
    this.path = path;
    this.format = format;
  }
  
  public Boolean isActive() {
    return active;
  }
  public String getPath() {
    return path;
  }
  public FilmlistFormats getFormat() {
    return format;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof final ImportLivestreamConfiguration that)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return Objects.equals(isActive(), that.isActive())
        && Objects.equals(getPath(), that.getPath())
        && Objects.equals(getFormat(), that.getFormat());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        isActive(),
        getPath(),
        getFormat());
  }
}
