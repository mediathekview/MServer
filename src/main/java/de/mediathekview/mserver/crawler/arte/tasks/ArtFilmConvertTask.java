package de.mediathekview.mserver.crawler.arte.tasks;

import java.lang.reflect.Type;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.arte.json.ArteFilmDeserializer;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractRecrusivConverterTask;

public class ArtFilmConvertTask extends AbstractRecrusivConverterTask<Film, JsonElement> {

  private static final Type OPTIONAL_FILM_TYPE = new TypeToken<Optional<Film>>() {}.getType();
  private static final long serialVersionUID = -7559130997870753602L;
  private final Gson gson;

  public ArtFilmConvertTask(final AbstractCrawler aCrawler,
      final ConcurrentLinkedQueue<JsonElement> aUrlToCrawlDTOs) {
    super(aCrawler, aUrlToCrawlDTOs);
    gson = new GsonBuilder()
        .registerTypeAdapter(OPTIONAL_FILM_TYPE, new ArteFilmDeserializer(crawler)).create();
  }

  @Override
  protected AbstractRecrusivConverterTask<Film, JsonElement> createNewOwnInstance(
      final ConcurrentLinkedQueue<JsonElement> aElementsToProcess) {
    return new ArtFilmConvertTask(crawler, aElementsToProcess);
  }

  @Override
  protected Integer getMaxElementsToProcess() {
    return config.getMaximumUrlsPerTask();
  }

  @Override
  protected void processElement(final JsonElement aElement) {
    final Optional<Film> film = gson.fromJson(aElement, OPTIONAL_FILM_TYPE);
    if (film.isPresent()) {
      taskResults.add(film.get());
      crawler.incrementAndGetActualCount();
      crawler.updateProgress();
    }
  }

}
