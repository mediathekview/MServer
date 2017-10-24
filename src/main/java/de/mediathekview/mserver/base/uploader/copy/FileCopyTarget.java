package de.mediathekview.mserver.base.uploader.copy;

import java.nio.file.Path;

import de.mediathekview.mserver.base.uploader.UploadTarget;

public class FileCopyTarget implements UploadTarget {
    private Path targetFolderPath;
    
    public FileCopyTarget(Path aTargetPath) {
        targetFolderPath = aTargetPath;
    }
    
    public Path getTargetPath() {
        return targetFolderPath;
    }
}
