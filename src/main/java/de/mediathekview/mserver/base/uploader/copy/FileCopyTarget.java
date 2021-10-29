package de.mediathekview.mserver.base.uploader.copy;

import de.mediathekview.mserver.base.uploader.UploadTarget;

import java.nio.file.Path;

public class FileCopyTarget implements UploadTarget {
  private final Path targetFolderPath;

  public FileCopyTarget(final Path aTargetPath) {
    targetFolderPath = aTargetPath;
  }

  public Path getTargetPath() {
    return targetFolderPath;
  }
}
