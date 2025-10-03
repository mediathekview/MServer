package de.mediathekview.mlib.filmlisten.writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.GsonDurationAdapter;
import de.mediathekview.mlib.daten.GsonLocalDateTimeAdapter;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

public class FilmlistWriter extends AbstractFilmlistWriter {

  public FilmlistWriter() {
    super();
  }

  public FilmlistWriter(final MessageListener... listeners) {
    super(listeners);
  }

  @Override
  public boolean write(Filmlist filmlist, OutputStream outputStream) throws IOException {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter());
    gsonBuilder.registerTypeAdapter(Duration.class, new GsonDurationAdapter());
    Gson gson = gsonBuilder.create();
    try (final BufferedWriter fileWriter =
        new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8), 512000)) {
      gson.toJson(filmlist, fileWriter);
      fileWriter.flush();
    }

    return true;
  }
}
