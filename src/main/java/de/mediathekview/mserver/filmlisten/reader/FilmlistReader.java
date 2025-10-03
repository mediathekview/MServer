package de.mediathekview.mserver.filmlisten.reader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.GsonDurationAdapter;
import de.mediathekview.mserver.daten.GsonLocalDateTimeAdapter;
import de.mediathekview.mserver.base.messages.LibMessages;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilmlistReader extends AbstractFilmlistReader {
	private static final Logger LOG = LogManager.getLogger(FilmlistReader.class);

	public FilmlistReader() {
		super();
	}

	public FilmlistReader(final MessageListener... aListeners) {
		super(aListeners);
	}

	@Override
	public Optional<Filmlist> read(InputStream aInputStream) {
    GsonBuilder gsonBuilder = new GsonBuilder();
    gsonBuilder.registerTypeAdapter(LocalDateTime.class, new GsonLocalDateTimeAdapter());
    gsonBuilder.registerTypeAdapter(Duration.class, new GsonDurationAdapter());
    Gson gson = gsonBuilder.create();
		try (JsonReader jsonReader = new JsonReader(
				new BufferedReader(new InputStreamReader(aInputStream, StandardCharsets.UTF_8.name())))) {
			return Optional.of(gson.fromJson(jsonReader, Filmlist.class));
		} catch (IOException ioException) {
			LOG.debug("Something went wrong on writing the film list.", ioException);
			publishMessage(LibMessages.FILMLIST_READ_ERROR);
			return Optional.empty();
		}
	}
}