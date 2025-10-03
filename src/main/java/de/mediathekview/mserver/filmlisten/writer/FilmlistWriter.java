package de.mediathekview.mserver.filmlisten.writer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.GsonDurationAdapter;
import de.mediathekview.mserver.daten.GsonLocalDateTimeAdapter;
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
