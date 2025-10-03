package de.mediathekview.mserver.base.config;

import java.util.Objects;

import de.mediathekview.mserver.filmlisten.FilmlistFormats;

public class ImportFilmlistConfiguration {
  private final Boolean active;
  private final String path;
  private final FilmlistFormats format;
  private final Boolean createDiff;
  private final Boolean checkImportListUrl;
  

  public ImportFilmlistConfiguration(Boolean active, String path, FilmlistFormats format, Boolean createDiff, Boolean checkImportListUrl) {
    this.active = active;
    this.path = path;
    this.format = format;
    this.createDiff = createDiff;
    this.checkImportListUrl = checkImportListUrl; 
  }
  
  public ImportFilmlistConfiguration() {
    this.active = null;
    this.path = null;
    this.format = null;
    this.createDiff = null;
    this.checkImportListUrl = null;
  }
  
  public Boolean isCheckImportListUrl() {
    return checkImportListUrl;
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
        && Objects.equals(isCreateDiff(), that.isCreateDiff()
        && Objects.equals(isCheckImportListUrl(), that.isCheckImportListUrl()));
  }
  
  @Override
  public int hashCode() {
    return Objects.hash(
        super.hashCode(),
        isActive(),
        getPath(),
        getFormat(),
        isCreateDiff(),
        isCheckImportListUrl());
  }
}
