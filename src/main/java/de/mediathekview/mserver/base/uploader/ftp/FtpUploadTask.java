package de.mediathekview.mserver.base.uploader.ftp;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.io.CopyStreamEvent;
import org.apache.commons.net.io.CopyStreamListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.uploader.UploadTask;

public class FtpUploadTask extends UploadTask<FtpUploadTarget> implements CopyStreamListener
{

    private static final Logger LOG = LogManager.getLogger(FtpUploadTask.class);

    public FtpUploadTask(final Path aSourcePath, final FtpUploadTarget aUploadTarget)
    {
        super(aSourcePath, aUploadTarget);
    }

    @Override
    protected void upload()
    {
        final FTPClient client = new FTPClient();
        client.setCopyStreamListener(this);
        try (InputStream fileInput = Files.newInputStream(sourcePath))
        {
            if (uploadTarget.getPort().isPresent())
            {
                client.connect(uploadTarget.getServerUrl().toExternalForm(), uploadTarget.getPort().get());
            }
            else
            {
                client.connect(uploadTarget.getServerUrl().toExternalForm());
            }
            if (uploadTarget.getUsername().isPresent() && uploadTarget.getPassword().isPresent())
            {
                client.login(uploadTarget.getUsername().get(), uploadTarget.getPassword().get());
            }

            client.storeFile(uploadTarget.getTargetPath(), fileInput);
            client.logout();
        }
        catch (final IOException ioException)
        {
            printMessage(ServerMessages.FILMLIST_FTP_UPLOAD_ERROR, sourcePath.toString(), uploadTarget.getTargetPath());
            LOG.fatal(String.format("Something went teribble wrong on uploading \"%s\" to \"%s\" via FTP.",
                    sourcePath.toString(), uploadTarget.getTargetPath()));
        }

    }

    @Override
    public void bytesTransferred(final CopyStreamEvent aEvent)
    {
        bytesTransferred(aEvent.getTotalBytesTransferred(), aEvent.getBytesTransferred(), aEvent.getStreamSize());
    }

    @Override
    public void bytesTransferred(final long aTotalBytesTransferred, final int aBytesTransferred, final long aStreamSize)
    {
        try
        {
            final Progress progress = new Progress(Files.size(sourcePath), aTotalBytesTransferred, 0);
            progressListeners.forEach(l -> l.updateProgess(progress));
        }
        catch (final IOException ioException)
        {
            printMessage(ServerMessages.FTP_FILE_SIZE_ERROR, sourcePath.toString());
            LOG.fatal(String.format("Something went teribble wrong on getting the size of \"%s\".",
                    sourcePath.toString()));
        }

    }

    protected void printMessage(final Message aMessage, final Object... args)
    {
        messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
    }

}
