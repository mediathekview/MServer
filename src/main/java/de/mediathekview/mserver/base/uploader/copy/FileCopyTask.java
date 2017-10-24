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

    @Override
    protected void upload() {
        try {
            Files.copy(sourcePath, uploadTarget.getTargetFolderPath(),StandardCopyOption.REPLACE_EXISTING);
        }catch(IOException ioException)
        {
            LOG.error("Something wen't wrong on copying the film lists.",ioException);
            printMessage(ServerMessages.FILE_COPY_ERROR);
        }
    }
    
    protected void printMessage(final Message aMessage, final Object... args) {
		messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
	}
}
