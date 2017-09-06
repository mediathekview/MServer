package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.DatenFilm;
import java.io.IOException;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.concurrent.Callable;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.newsearch.GeoLocations;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.tool.MserverDatumZeit;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Liest anhand einer ProgramId die Daten eines Films
 */
public class ArteProgramIdToDatenFilmCallable implements Callable<DatenFilm> {
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
    public DatenFilm call() throws Exception {
        DatenFilm film = null;
        
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer())
                .registerTypeAdapter(ArteVideoDetailsDTO.class, new ArteVideoDetailsDeserializer(today))
                .create();

        String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
        ArteVideoDTO video = ArteHttpClient.executeRequest(LOG, gson, videosUrl, ArteVideoDTO.class);

        if(video != null) {
            //The duration as time so it can be formatted and co.
            LocalTime durationAsTime = durationAsTime(video.getDurationInSeconds());

            if (video.getVideoUrls().containsKey(Qualities.NORMAL))
            {
                ArteVideoDetailsDTO details = getVideoDetails(gson, programId);                      
                if(details != null) {
                    film = createFilm(details.getTheme(), details.getWebsite(), details.getTitle(), video, details, durationAsTime, details.getDescription());
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
    
    private DatenFilm createFilm(String thema, String urlWeb, String titel, ArteVideoDTO video, ArteVideoDetailsDTO details, LocalTime durationAsTime, String beschreibung) {
        
        String broadcastBegin = details.getBroadcastBegin();
        String date = MserverDatumZeit.formatDate(broadcastBegin, broadcastDateFormat);
        String time = MserverDatumZeit.formatTime(broadcastBegin, broadcastDateFormat);
        
        DatenFilm film = new DatenFilm(senderName, thema, urlWeb, titel, video.getUrl(Qualities.NORMAL), "" /*urlRtmp*/,
                date, time, durationAsTime.toSecondOfDay(), beschreibung);
        if (video.getVideoUrls().containsKey(Qualities.HD))
        {
            CrawlerTool.addUrlHd(film, video.getUrl(Qualities.HD), "");
        }
        if (video.getVideoUrls().containsKey(Qualities.SMALL))
        {
            CrawlerTool.addUrlKlein(film, video.getUrl(Qualities.SMALL), "");
        }

        if (details.getGeoLocation() != GeoLocations.GEO_NONE) {
            film.arr[DatenFilm.FILM_GEO] = details.getGeoLocation().getDescription();
        }

        return film;
    }
    
    private LocalTime durationAsTime(long aDurationInSeconds)
    {
        LocalTime localTime = LocalTime.MIN;
        
        localTime = localTime.plusSeconds(aDurationInSeconds);
        
        return localTime;
    }    
}
