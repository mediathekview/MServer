package de.mediathekview.mserver.filmlisten.writer;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mserver.base.messages.LibMessages;
import de.mediathekview.mserver.base.messages.MessageCreator;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class AbstractFilmlistWriter extends MessageCreator {
  private static final Logger LOG = LogManager.getLogger(AbstractFilmlistWriter.class);

  protected AbstractFilmlistWriter() {
    super();
  }

  protected AbstractFilmlistWriter(final MessageListener... aListeners) {
    super(aListeners);
  }

  public abstract boolean write(Filmlist filmlist, OutputStream outputStream) throws IOException;

  public boolean write(Filmlist filmlist, Path savePath) {
    try (final OutputStream os = new FileOutputStream(savePath.toFile());
         final BufferedOutputStream fos = new BufferedOutputStream(os, 512000)) {
      return write(filmlist, fos);
    } catch (final IOException ioException) {
      LOG.debug("Something went wrong on writing the film list.", ioException);
      publishMessage(LibMessages.FILMLIST_WRITE_ERROR, savePath.toAbsolutePath().toString());
      return false;
    }
  }
}
