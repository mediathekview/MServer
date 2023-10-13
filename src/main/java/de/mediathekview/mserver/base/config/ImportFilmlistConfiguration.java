package de.mediathekview.mserver.base.config;

import java.util.Objects;

import de.mediathekview.mlib.filmlisten.FilmlistFormats;

public class ImportFilmlistConfiguration {
  private final Boolean active;
  private final String path;
  private final FilmlistFormats format;
  private final Boolean createDiff;
  

  public ImportFilmlistConfiguration(Boolean active, String path, FilmlistFormats format, Boolean createDiff) {
    this.active = active;
    this.path = path;
    this.format = format;
    this.createDiff = createDiff;
  }
  
  public ImportFilmlistConfiguration() {
    this.active = null;
    this.path = null;
    this.format = null;
    this.createDiff = null;
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
  public Boolean isCreateDiff() {
    return createDiff;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (!(obj instanceof final ImportFilmlistConfiguration that)) {
      return false;
    }
    if (!super.equals(obj)) {
      return false;
    }
    return Objects.equals(isActive(), that.isActive())
        && Objects.equals(getPath(), that.getPath())
        && Objects.equals(getFormat(), that.getFormat())
        && Objects.equals(isCreateDiff(), that.isCreateDiff());
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        isActive(),
        getPath(),
        getFormat(),
        isCreateDiff());
  }
}
