package de.mediathekview.mserver.filmlisten;

import de.mediathekview.mlib.compression.CompressionManager;
import de.mediathekview.mlib.compression.CompressionType;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.filmlisten.reader.FilmlistOldFormatReader;
import de.mediathekview.mlib.filmlisten.reader.FilmlistReader;
import de.mediathekview.mlib.filmlisten.writer.AbstractFilmlistWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistOldFormatWriter;
import de.mediathekview.mlib.filmlisten.writer.FilmlistWriter;
import de.mediathekview.mserver.base.messages.LibMessages;
import de.mediathekview.mserver.base.messages.MessageCreator;
import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Optional;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilmlistManager extends MessageCreator {

  private static final Logger LOG = LogManager.getLogger(FilmlistManager.class);
  private static final String TEMP_ENDING = "_TEMP";
  private static FilmlistManager instance;


  private FilmlistManager() {
    super();
  }

  public static FilmlistManager getInstance() {
    if (instance == null) {
      instance = new FilmlistManager();
    }
    return instance;
  }

  private Optional<Filmlist> importList(
      final FilmlistFormats aFormat, final InputStream aInputStream) throws IOException {
    publishMessage(LibMessages.FILMLIST_IMPORT_STARTED);
    final InputStream input = decompressInputStreamIfFormatNeedsTo(aFormat, aInputStream);

    try {
      if (aFormat.isOldFormat()) {
        return new FilmlistOldFormatReader().read(input);
      } else {
        return new FilmlistReader().read(input);
      }
    } finally {
      publishMessage(LibMessages.FILMLIST_IMPORT_FINISHED);
    }
  }

  private InputStream decompressInputStreamIfFormatNeedsTo(
      final FilmlistFormats aFormat, final InputStream aInputStream) throws IOException {
    final InputStream input;
    final Optional<CompressionType> compressionType = aFormat.getCompressionType();
    if (compressionType.isPresent()) {
      input = CompressionManager.getInstance().decompress(compressionType.get(), aInputStream);
    } else {
      input = aInputStream;
    }
    return input;
  }

  public Optional<Filmlist> importList(final FilmlistFormats aFormat, final Path aFilePath)
      throws IOException {
    try (final InputStream fileInputStream = Files.newInputStream(aFilePath);
        final BufferedInputStream bis = new BufferedInputStream(fileInputStream, 512000) ) {
      return importList(aFormat, bis);
    }
  }

  public Optional<Filmlist> importList(final FilmlistFormats aFormat, final URL aUrl)
      throws IOException {
    final Request request = new Request.Builder().url(aUrl).build();
    final OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();

    try (final Response response = httpClient.newCall(request).execute()) {
      final ResponseBody responseBody = response.body();
      if (responseBody == null) {
        return Optional.empty();
      }
      try (final InputStream fileInputStream = responseBody.byteStream();
          final BufferedInputStream bis = new BufferedInputStream(fileInputStream, 512000)) {
        return importList(aFormat, bis);
      }
    }
  }

  public boolean save(
      final FilmlistFormats aFormat, final Filmlist aFilmlist, final Path aSavePath) {
    try {
      publishMessage(LibMessages.FILMLIST_WRITE_STARTED, aSavePath);
      if (aFormat.isOldFormat()) {
        final FilmlistOldFormatWriter filmlistOldFormatWriter = new FilmlistOldFormatWriter();
        filmlistOldFormatWriter.addAllMessageListener(messageListeners);
        return save(filmlistOldFormatWriter, aFormat, aFilmlist, aSavePath);
      } else {
        final FilmlistWriter filmlistWriter = new FilmlistWriter();
        filmlistWriter.addAllMessageListener(messageListeners);
        return save(filmlistWriter, aFormat, aFilmlist, aSavePath);
      }
    } finally {
      publishMessage(LibMessages.FILMLIST_WRITE_FINISHED, aSavePath);
    }
  }

  private boolean compress(
      final FilmlistFormats aFormat, final Path aSourcePath, final Path aTargetPath) {
    final Optional<CompressionType> compressionType = aFormat.getCompressionType();
    if (compressionType.isPresent()) {
      try {
        CompressionManager.getInstance().compress(compressionType.get(), aSourcePath, aTargetPath);
        return true;
      } catch (final IOException ioException) {
        publishMessage(
            LibMessages.FILMLIST_COMPRESS_ERROR, aTargetPath.toAbsolutePath().toString());
      }
    }
    return false;
  }

  public boolean writeHashFile(final Filmlist filmlist, final Path savePath) {
    try (final BufferedWriter fileWriter =
        Files.newBufferedWriter(
            savePath,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      fileWriter.write(String.valueOf(filmlist.hashCode()));
      return true;
    } catch (final IOException ioException) {
      LOG.fatal("Can't write the hash file \"{}\".", savePath);
      return false;
    }
  }

  public boolean writeIdFile(final Filmlist filmlist, final Path savePath) {
    try (final BufferedWriter fileWriter =
        Files.newBufferedWriter(
            savePath,
            StandardCharsets.UTF_8,
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {
      fileWriter.write(filmlist.getListId().toString());
      return true;
    } catch (final IOException ioException) {
      LOG.fatal("Can't write the hash file \"{}\".", savePath);
      return false;
    }
  }

  private boolean compressFile(
      final AbstractFilmlistWriter aWriter,
      final FilmlistFormats aFormat,
      final Path aSavePath,
      final Filmlist aFilmlist) {
    final Path tempPath =
        aSavePath.resolveSibling(aSavePath.getFileName().toString() + TEMP_ENDING);
    try {
      return aWriter.write(aFilmlist, tempPath) && compress(aFormat, tempPath, aSavePath);
    } finally {
      try {
        Files.deleteIfExists(tempPath);
      } catch (final IOException ioException) {
        LOG.error(String.format("Can't delete temp file \"%s\".", tempPath.toString()));
      }
    }
  }

  private boolean save(
      final AbstractFilmlistWriter aFilmlistwirter,
      final FilmlistFormats aFormat,
      final Filmlist aFilmlist,
      final Path aSavePath) {
    if (aFormat.getCompressionType().isPresent()) {
      return compressFile(aFilmlistwirter, aFormat, aSavePath, aFilmlist);
    } else {
      return aFilmlistwirter.write(aFilmlist, aSavePath);
    }
  }
}
