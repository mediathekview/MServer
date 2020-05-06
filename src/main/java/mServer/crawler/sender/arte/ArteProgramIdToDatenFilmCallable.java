package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.DatenFilm;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.base.GeoLocations;
import mServer.crawler.sender.base.Qualities;
import mServer.tool.MserverDatumZeit;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Liest anhand einer ProgramId die Daten eines Films
 */
public class ArteProgramIdToDatenFilmCallable implements Callable<Set<DatenFilm>> {

  private static final Logger LOG = LogManager.getLogger(ArteProgramIdToDatenFilmCallable.class);

  private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";
  private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN_2 = "https://api.arte.tv/api/opa/v3/programs/%s/%s"; // Für broadcastBeginRounded

  private final FastDateFormat broadcastDateFormat = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssX");//2016-10-29T16:15:00Z

  private final String programId;
  private final String langCode;
  private final String senderName;

  private final Calendar today;

  public ArteProgramIdToDatenFilmCallable(String aProgramId, String aLangCode, String aSenderName) {
    programId = aProgramId;
    langCode = aLangCode;
    senderName = aSenderName;
    today = Calendar.getInstance();
  }

  @Override
  public Set<DatenFilm> call() throws Exception {
    Set<DatenFilm> films = new HashSet<>();

    Gson gson = new GsonBuilder()
            .registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer(senderName))
            .registerTypeAdapter(ArteVideoDetailsDTO.class, new ArteVideoDetailsDeserializer(today))
            .create();

    String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
    ArteVideoDTO video = ArteHttpClient.executeRequest(LOG, gson, videosUrl, ArteVideoDTO.class);

    if (video != null) {
      //The duration as time so it can be formatted and co.
      LocalTime durationAsTime = durationAsTime(video.getDurationInSeconds());

      if (video.getVideoUrls().containsKey(Qualities.NORMAL)) {
        ArteVideoDetailsDTO details = getVideoDetails(gson, programId);
        if (details != null) {
          films.add(createFilm(details.getTheme(), details.getWebsite(), details.getTitle(), video.getVideoUrls(), details, durationAsTime, details.getDescription()));

          if (!video.getVideoUrlsWithAudioDescription().isEmpty()) {
            films.add(createFilm(details.getTheme(), details.getWebsite(), details.getTitle() + " (Hörfilm)", video.getVideoUrlsWithAudioDescription(), details, durationAsTime, details.getDescription()));
          }
          if (!video.getVideoUrlsWithSubtitle().isEmpty()) {
            films.add(createFilm(details.getTheme(), details.getWebsite(), details.getTitle() + " (Hörfassung)", video.getVideoUrlsWithSubtitle(), details, durationAsTime, details.getDescription()));
          }
        }
      } else {
        LOG.debug(String.format("Keine \"normale\" Video URL für den Film \"%s\"", programId));
      }
    }

    return films;
  }

  private ArteVideoDetailsDTO getVideoDetails(Gson gson, String programId) throws IOException {

    //https://api.arte.tv/api/opa/v3/programs/[language:de/fr]/[programId]
    String videosUrlVideoDetails2 = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN_2, langCode, programId);
    ArteVideoDetailsDTO details = ArteHttpClient.executeRequest(LOG, gson, videosUrlVideoDetails2, ArteVideoDetailsDTO.class);
    return details;
  }

  private DatenFilm createFilm(String thema, String urlWeb, String titel, Map<Qualities, String> videos, ArteVideoDetailsDTO details, LocalTime durationAsTime, String beschreibung) {

    String broadcastBegin = details.getBroadcastBegin();
    String date = MserverDatumZeit.formatDate(broadcastBegin, broadcastDateFormat);
    String time = MserverDatumZeit.formatTime(broadcastBegin, broadcastDateFormat);

    DatenFilm film = new DatenFilm(senderName, thema, urlWeb, titel, videos.get(Qualities.NORMAL), "" /*urlRtmp*/,
            date, time, durationAsTime.toSecondOfDay(), beschreibung);
    if (videos.containsKey(Qualities.HD)) {
      CrawlerTool.addUrlHd(film, videos.get(Qualities.HD));
    }
    if (videos.containsKey(Qualities.SMALL)) {
      CrawlerTool.addUrlKlein(film, videos.get(Qualities.SMALL));
    }

    if (details.getGeoLocation() != GeoLocations.GEO_NONE) {
      film.arr[DatenFilm.FILM_GEO] = details.getGeoLocation().getDescription();
    }

    return film;
  }

  private LocalTime durationAsTime(long aDurationInSeconds) {
    LocalTime localTime = LocalTime.MIN;

    localTime = localTime.plusSeconds(aDurationInSeconds);

    return localTime;
  }
}
