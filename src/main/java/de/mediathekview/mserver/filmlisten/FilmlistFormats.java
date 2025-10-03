package de.mediathekview.mserver.filmlisten;

import de.mediathekview.mserver.base.compression.CompressionType;
import java.util.Optional;

public enum FilmlistFormats {
  JSON("Json", false, "json"),
  OLD_JSON("Old Json", true, "json"),
  JSON_COMPRESSED_XZ("Json + XZ", false, CompressionType.XZ),
  OLD_JSON_COMPRESSED_XZ("Old Json compressed XZ", true, CompressionType.XZ),
  JSON_COMPRESSED_GZIP("Json + GZIP", false, CompressionType.GZIP),
  OLD_JSON_COMPRESSED_GZIP("Old Json compressed GZIP", true, CompressionType.GZIP),
  JSON_COMPRESSED_BZIP("Json + BZIP", false, CompressionType.BZIP),
  OLD_JSON_COMPRESSED_BZIP("Old Json compressed BZIP", true, CompressionType.BZIP);

  private final String description;
  private final boolean oldFormat;
  private final Optional<String> fileExtension;
  private final Optional<CompressionType> compressionType;

  FilmlistFormats(
      final String aDescription, final boolean aOldFormat, final CompressionType aCompressionType) {
    description = aDescription;
    oldFormat = aOldFormat;
    compressionType = Optional.of(aCompressionType);
    fileExtension = Optional.empty();
  }

  FilmlistFormats(
      final String aDescription, final boolean aOldFormat, final String aFileExtension) {
    description = aDescription;
    oldFormat = aOldFormat;
    fileExtension = Optional.of(aFileExtension);
    compressionType = Optional.empty();
  }

  public static Optional<FilmlistFormats> getByDescription(final String aDescription) {
    for (final FilmlistFormats format : values()) {
      if (format.getDescription().equals(aDescription)) {
        return Optional.of(format);
      }
    }
    return Optional.empty();
  }

  public Optional<CompressionType> getCompressionType() {
    return compressionType;
  }

  public String getDescription() {
    return description;
  }

  public String getFileExtension() {
    if (fileExtension.isPresent()) {
      return fileExtension.get();
    }
    if (compressionType.isPresent()) {
      return compressionType.get().getFileEnding();
    }
    return "";
  }

  public boolean isOldFormat() {
    return oldFormat;
  }
}
