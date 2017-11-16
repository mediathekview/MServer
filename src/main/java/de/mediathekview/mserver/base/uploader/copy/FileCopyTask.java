package de.mediathekview.mserver.base.uploader.copy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.uploader.UploadTask;

public class FileCopyTask extends UploadTask<FileCopyTarget> {
  private static final Logger LOG = LogManager.getLogger(FileCopyTask.class);

  public FileCopyTask(final Path aSourcePath, final FileCopyTarget aCopyTarget) {
    super(aSourcePath, aCopyTarget);
  }

  protected void printMessage(final Message aMessage, final Object... args) {
    messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
  }

  @Override
  protected void upload() {
    try {
      if (Files.exists(uploadTarget.getTargetPath())) {
        printMessage(ServerMessages.FILE_COPY_TARGET_EXISTS,
            uploadTarget.getTargetPath().toAbsolutePath().toString());
      }
      Files.copy(sourcePath, uploadTarget.getTargetPath(), StandardCopyOption.REPLACE_EXISTING);
    } catch (final IOException ioException) {
      LOG.error("Something wen't wrong on copying the film list.", ioException);
      printMessage(ServerMessages.FILE_COPY_ERROR);
    }
  }
}
