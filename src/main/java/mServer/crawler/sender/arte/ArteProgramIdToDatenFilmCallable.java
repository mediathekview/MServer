package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import mServer.crawler.CrawlerTool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Liest anhand einer ProgramId die Daten eines Films
 */
public class ArteProgramIdToDatenFilmCallable implements Callable<Film> {
    private static final Logger LOG = LogManager.getLogger(ArteProgramIdToDatenFilmCallable.class);

    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN_2 = "https://api.arte.tv/api/opa/v3/programs/%s/%s"; // Für broadcastBeginRounded
    
    private final String programId;
    private final String langCode;
    private final Sender sender;
    
    public ArteProgramIdToDatenFilmCallable(String aProgramId, String aLangCode, Sender aSender) {
        programId = aProgramId;
        langCode = aLangCode;
        sender = aSender;
    }
    
    @Override
    public Film call() throws Exception {
        Film film = null;
        
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer())
                .registerTypeAdapter(ArteVideoDetailsDTO.class, new ArteVideoDetailsDeserializer())
                .create();

        String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
        ArteVideoDTO video = ArteHttpClient.executeRequest(LOG, gson, videosUrl, ArteVideoDTO.class);

        if(video != null) {
            //The duration as time so it can be formatted and co.
            Duration duration = Duration.of(video.getDurationInSeconds(), ChronoUnit.SECONDS);

            if (video.getVideoUrls().containsKey(Qualities.NORMAL))
            {
                ArteVideoDetailsDTO details = getVideoDetails(gson, programId);                      
                if(details != null) {
                    film = createFilm(details.getTheme(), details.getWebsite(), details.getTitle(), video, details, duration, details.getDescription());
                }
           } else {
               LOG.debug(String.format("Keine \"normale\" Video URL für den Film \"%s\"", programId));
           }
        }

        return film;
    }

    private ArteVideoDetailsDTO getVideoDetails(Gson gson, String programId) throws IOException {
        
        //https://api.arte.tv/api/opa/v3/programs/[language:de/fr]/[programId]
        String videosUrlVideoDetails2 = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN_2, langCode, programId);
        ArteVideoDetailsDTO details = ArteHttpClient.executeRequest(LOG, gson, videosUrlVideoDetails2, ArteVideoDetailsDTO.class);
        return details;
    }
    
    private Film createFilm(String thema, String urlWeb, String titel, ArteVideoDTO video, ArteVideoDetailsDTO details, Duration duration, String beschreibung) throws URISyntaxException {
        
        Collection<GeoLocations> geoLocations = CrawlerTool.getGeoLocations(sender, video.getUrl(Qualities.NORMAL));
        if (details.getGeoLocation() != GeoLocations.GEO_NONE) {
            geoLocations.remove(GeoLocations.GEO_NONE);
            geoLocations.add(details.getGeoLocation());
        }
        
        Film film = new Film(UUID.randomUUID(), geoLocations, sender, titel, thema, details.getBroadcastBegin(), duration, new URI(urlWeb));
        film.setBeschreibung(beschreibung);
        for(Qualities quality : video.getVideoUrls().keySet())
        {
            film.addUrl(quality, CrawlerTool.stringToFilmUrl(video.getUrl(quality)));
        }                

        return film;
    }
}
