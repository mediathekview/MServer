package de.mediathekview.mserver.base.uploader.copy;

import de.mediathekview.mserver.base.messages.Message;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.uploader.UploadTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

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
      Path target = uploadTarget.getTargetPath();
      if (Files.exists(target)) {
        Path backup = backupExistingFile(target);
        LOG.debug("CopyTask found existing file - rename existing file to {} before overwrite", backup.getFileName());
      }
      Path tmpTarget = Files.createTempFile( uploadTarget.getTargetPath().getParent(), uploadTarget.getTargetPath().getFileName().toString(), ".tmp");
      Files.copy(sourcePath, tmpTarget, StandardCopyOption.REPLACE_EXISTING);
      Files.move(tmpTarget, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
    } catch (final IOException ioException) {
      LOG.error("Something went wrong on copying the film list.", ioException);
      printMessage(ServerMessages.FILE_COPY_ERROR);
    }
  }
  
  private Path backupExistingFile(Path target) throws IOException {
    String fileName = target.getFileName().toString();
    Path dir = target.getParent();

    String date = LocalDate.now()
        .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

    Path backup = dir.resolve(fileName + "." + date);

    int counter = 1;
    while (Files.exists(backup)) {
      backup = dir.resolve(fileName + "." + date + "." + counter);
      counter++;
    }

    Files.move(target, backup);
    
    return backup;
  }

}
