package mServer.crawler.sender.arte;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.Callable;

import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.newsearch.Qualities;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.tool.MserverDatumZeit;
import org.apache.commons.lang3.time.FastDateFormat;

public class ArteJsonObjectToDatenFilmCallable implements Callable<DatenFilm>
{
    private static final Logger LOG = LogManager.getLogger(ArteJsonObjectToDatenFilmCallable.class);
    private static final String JSON_OBJECT_KEY_PROGRAM = "program";
    private static final String JSON_ELEMENT_KEY_TITLE = "title";
    private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_KEY_URL = "url";
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s";
    private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";
    private static final String JSON_ELEMENT_BROADCAST = "broadcastBeginRounded";
    
    private final JsonObject jsonObject;
    private final String langCode;
    private final String senderName;
    
    private final FastDateFormat broadcastDate = FastDateFormat.getInstance("yyyy-MM-dd'T'HH:mm:ssX");//2016-10-29T16:15:00Z
    
    public ArteJsonObjectToDatenFilmCallable(JsonObject aJsonObjec, String aLangCode, String aSenderName) {
        jsonObject = aJsonObjec;
        langCode = aLangCode;
        senderName = aSenderName;
    }

    @Override
    public DatenFilm call() {
        DatenFilm film = null;
        if(jsonObject != null && jsonObject.has(JSON_OBJECT_KEY_PROGRAM))
        {
            JsonObject programObject = jsonObject.get(JSON_OBJECT_KEY_PROGRAM).getAsJsonObject();
            if(programObject.has(JSON_ELEMENT_KEY_TITLE) && 
            programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID) && 
            programObject.has(JSON_ELEMENT_KEY_URL) &&
            !programObject.get(JSON_ELEMENT_KEY_TITLE).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_URL).isJsonNull()
            )
            {
                String thema = !programObject.get(JSON_ELEMENT_KEY_TITLE).isJsonNull() ? programObject.get(JSON_ELEMENT_KEY_TITLE).getAsString() : "";
                String titel = !programObject.get(JSON_ELEMENT_KEY_SUBTITLE).isJsonNull() ? programObject.get(JSON_ELEMENT_KEY_SUBTITLE).getAsString() : "";
                String beschreibung = !programObject.get(JSON_ELEMENT_KEY_SHORT_DESCRIPTION).isJsonNull() ? programObject.get(JSON_ELEMENT_KEY_SHORT_DESCRIPTION).getAsString() : "";
                
                String urlWeb = programObject.get(JSON_ELEMENT_KEY_URL).getAsString();
    
                //https://api.arte.tv/api/player/v1/config/[language:de/fr]/[programId]
                String programId = programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).getAsString();
                String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
                
                Gson gson = new GsonBuilder().registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer()).create();
    
                OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
                Request requestVideoDetails = new Request.Builder().url(videosUrl).build();
                
                try(Response responseVideoDetails = httpClient.newCall(requestVideoDetails).execute();)
                {
                    if(responseVideoDetails.isSuccessful())
                    {
                        ArteVideoDTO video = gson.fromJson(responseVideoDetails.body().string(), ArteVideoDTO.class);
                        
                        //The duration as time so it can be formatted and co.
                        LocalTime durationAsTime = durationAsTime(video.getDurationInSeconds());
                       
                        if (video.getVideoUrls().containsKey(Qualities.NORMAL))
                        {
                            String date = ""; 
                            String time = "";
                            
                            if(jsonObject.has(JSON_ELEMENT_BROADCAST)) {
                                String broadcastBegin = jsonObject.get(JSON_ELEMENT_BROADCAST).getAsString();
    
                                date = MserverDatumZeit.formatDate(broadcastBegin, broadcastDate);
                                time = MserverDatumZeit.formatTime(broadcastBegin, broadcastDate);
                            }
                            
                            film = new DatenFilm(senderName, thema, urlWeb, titel, video.getUrl(Qualities.NORMAL), "" /*urlRtmp*/,
                                    date, time, durationAsTime.toSecondOfDay(), beschreibung);
                            if (video.getVideoUrls().containsKey(Qualities.HD))
                            {
                                CrawlerTool.addUrlHd(film, video.getUrl(Qualities.HD), "");
                            }
                            if (video.getVideoUrls().containsKey(Qualities.SMALL))
                            {
                                CrawlerTool.addUrlKlein(film, video.getUrl(Qualities.SMALL), "");
                            }
                       }else {
                           LOG.debug(String.format("Keine \"normale\" Video URL für den Film \"%s\" mit der URL \"%s\". Video Details URL:\"%s\" ",titel,urlWeb,videosUrl));
                       }

                    }

                } catch (IOException ioException)
                {
                    LOG.error("Beim laden der Informationen eines Filmes für Arte kam es zu Verbindungsproblemen.", ioException);
                }
            }

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
