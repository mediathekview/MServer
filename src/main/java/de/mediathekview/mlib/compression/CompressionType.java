package de.mediathekview.mlib.compression;

public enum CompressionType {
  XZ(".xz"),
  GZIP(".gz"),
  BZIP(".bz");

  private final String fileEnding;

  CompressionType(final String aFileEnding) {
    fileEnding = aFileEnding;
  }

  public String getFileEnding() {
    return fileEnding;
  }
}
