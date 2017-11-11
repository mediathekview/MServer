package mServer.crawler.sender.arte;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import mServer.crawler.CrawlerTool;

/**
 * Liest anhand einer ProgramId die Daten eines Films
 */
public class ArteProgramIdToFilmCallable implements Callable<Film> {
  private static final Logger LOG = LogManager.getLogger(ArteProgramIdToFilmCallable.class);

  private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN =
      "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";
  private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN_2 =
      "https://api.arte.tv/api/opa/v3/programs/%s/%s"; // Für broadcastBeginRounded

  private final String programId;
  private final String langCode;
  private final Sender sender;

  public ArteProgramIdToFilmCallable(final String aProgramId, final String aLangCode,
      final Sender aSender) {
    programId = aProgramId;
    langCode = aLangCode;
    sender = aSender;
  }

  @Override
  public Film call() throws Exception {
    Film film = null;

    final Gson gson =
        new GsonBuilder().registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer())
            .registerTypeAdapter(ArteVideoDetailsDTO.class, new ArteVideoDetailsDeserializer())
            .create();

    final String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
    final ArteVideoDTO video =
        ArteHttpClient.executeRequest(LOG, gson, videosUrl, ArteVideoDTO.class);

    if (video != null) {
      // The duration as time so it can be formatted and co.
      final Duration duration = Duration.of(video.getDurationInSeconds(), ChronoUnit.SECONDS);

      if (video.getVideoUrls().containsKey(Resolution.NORMAL)) {
        final ArteVideoDetailsDTO details = getVideoDetails(gson, programId);
        if (details != null) {
          film = createFilm(details.getTheme(), details.getWebsite(), details.getTitle(), video,
              details, duration, details.getDescription());
        }
      } else {
        LOG.debug(String.format("Keine \"normale\" Video URL für den Film \"%s\"", programId));
      }
    }

    return film;
  }

  private Film createFilm(final String thema, final String urlWeb, final String titel,
      final ArteVideoDTO video, final ArteVideoDetailsDTO details, final Duration duration,
      final String beschreibung) throws URISyntaxException {

    final Collection<GeoLocations> geoLocations =
        CrawlerTool.getGeoLocations(sender, video.getUrl(Resolution.NORMAL));
    if (details.getGeoLocation() != GeoLocations.GEO_NONE) {
      geoLocations.remove(GeoLocations.GEO_NONE);
      geoLocations.add(details.getGeoLocation());
    }

    final Film film =
        new Film(UUID.randomUUID(), sender, titel, thema, details.getBroadcastBegin(), duration);
    film.addAllGeoLocations(geoLocations);
    try {
      film.setWebsite(new URL(urlWeb));
    } catch (final MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    film.setBeschreibung(beschreibung);
    for (final Resolution quality : video.getVideoUrls().keySet()) {
      try {
        film.addUrl(quality, CrawlerTool.stringToFilmUrl(video.getUrl(quality)));
      } catch (final MalformedURLException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      }
    }

    return film;
  }

  private ArteVideoDetailsDTO getVideoDetails(final Gson gson, final String programId)
      throws IOException {

    // https://api.arte.tv/api/opa/v3/programs/[language:de/fr]/[programId]
    final String videosUrlVideoDetails2 =
        String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN_2, langCode, programId);
    final ArteVideoDetailsDTO details =
        ArteHttpClient.executeRequest(LOG, gson, videosUrlVideoDetails2, ArteVideoDetailsDTO.class);
    return details;
  }
}
