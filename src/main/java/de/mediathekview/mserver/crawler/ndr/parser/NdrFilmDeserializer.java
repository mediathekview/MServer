package de.mediathekview.mserver.crawler.ndr.parser;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.ard.json.ArdMediaArrayToDownloadUrlsConverter;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import mServer.crawler.CrawlerTool;

public class NdrFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  private static final String ELEMENT_DURATION = "duration";
  private static final String ELEMENT_MEDIA_ARRAY = "_mediaArray";
  private static final Logger LOG = LogManager.getLogger(NdrFilmDeserializer.class);
  private final AbstractCrawler crawler;
  private final String title;
  private final String thema;
  private final LocalDateTime time;
  private final String url;

  public NdrFilmDeserializer(final AbstractCrawler aCrawler, final String aTitel, final String aURL,
      final String aThema, final LocalDateTime aTime) {
    crawler = aCrawler;
    title = aTitel;
    url = aURL;
    thema = aThema;
    time = aTime;
  }

  @Override
  public Optional<Film> deserialize(final JsonElement aElement, final Type aType,
      final JsonDeserializationContext aContext) {
    if (JsonUtils.hasElements(aElement, Optional.of(crawler), ELEMENT_MEDIA_ARRAY,
        ELEMENT_DURATION)) {
      final JsonObject baseObj = aElement.getAsJsonObject();

      final Duration dauer =
          Duration.of(baseObj.get(ELEMENT_DURATION).getAsLong(), ChronoUnit.SECONDS);

      final Film newFilm =
          new Film(UUID.randomUUID(), crawler.getSender(), title, thema, time, dauer);

      ArdMediaArrayToDownloadUrlsConverter.toDownloadUrls(baseObj.get(ELEMENT_MEDIA_ARRAY), crawler)
          .entrySet()
          .forEach(e -> newFilm.addUrl(e.getKey(), CrawlerTool.uriToFilmUrl(e.getValue())));

      final Optional<FilmUrl> defaultUrl = newFilm.getDefaultUrl();
      if (defaultUrl.isPresent()) {
        newFilm.setGeoLocations(
            CrawlerTool.getGeoLocations(crawler.getSender(), defaultUrl.get().getUrl().toString()));
      }

      try {
        newFilm.setWebsite(new URL(url));
      } catch (final MalformedURLException malformedURLException) {
        // I don't know why how and when this can happen but you know.
        LOG.fatal("Something went terrible wrong on converting the actual website url to a url.",
            malformedURLException);
      }
      return Optional.of(newFilm);
    }
    return Optional.empty();
  }

}
