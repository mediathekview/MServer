package de.mediathekview.mserver.filmlisten.writer;

import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.filmlisten.FilmListMessages;
import de.mediathekview.mserver.base.messages.MessageCreator;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
    try {
      Path tmpTarget = Files.createTempFile( savePath.getParent(), savePath.getFileName().toString(), ".tmp");
          try (final OutputStream os = new FileOutputStream(tmpTarget.toFile());
              final BufferedOutputStream fos = new BufferedOutputStream(os, 512000)) {
           boolean succuess =  write(filmlist, fos);
           fos.close();
           if (succuess) {
             Files.move(tmpTarget, savePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
           }
           return succuess;
         }
    } catch (final IOException ioException) {
      LOG.debug("Something went wrong on writing the film list.", ioException);
      publishMessage(FilmListMessages.FILMLIST_WRITE_ERROR, savePath.toAbsolutePath().toString());
      return false;
    }
  }
}
