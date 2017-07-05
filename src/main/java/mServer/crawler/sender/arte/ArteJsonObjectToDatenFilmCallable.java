package mServer.crawler.sender.arte;

import java.io.IOException;
import java.time.LocalTime;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.newsearch.Qualities;
import mServer.tool.MserverDatumZeit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class ArteJsonObjectToDatenFilmCallable implements Callable<DatenFilm>
{
    private static final Logger LOG = LogManager.getLogger(ArteJsonObjectToDatenFilmCallable.class);
    private static final String JSON_ELEMENT_KEY_CATEGORY = "category";
    private static final String JSON_ELEMENT_KEY_SUBCATEGORY = "subcategory";
    private static final String JSON_ELEMENT_KEY_NAME = "name";
    private static final String JSON_ELEMENT_KEY_TITLE = "title";
    private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_KEY_URL = "url";
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN_2 = "https://api.arte.tv/api/opa/v3/programs/%s/%s";
    private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";
//    private static final String JSON_ELEMENT_BROADCAST = "broadcastBegin";
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
        try {
        if(isValidProgramObject(jsonObject))
        {
            String titel = getTitle(jsonObject);
            String thema = getSubject(jsonObject);

            String beschreibung = getElementValue(jsonObject, JSON_ELEMENT_KEY_SHORT_DESCRIPTION);

            String urlWeb = getElementValue(jsonObject, JSON_ELEMENT_KEY_URL);

            //https://api.arte.tv/api/player/v1/config/[language:de/fr]/[programId]
            String programId = getElementValue(jsonObject, JSON_ELEMENT_KEY_PROGRAM_ID);
            String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
            String videosUrlVideoDetails2 = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN_2, langCode, programId);

            Gson gson = new GsonBuilder().registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer()).create();

            try(Response responseVideoDetails = executeRequest(videosUrl))
            {
                if(responseVideoDetails.isSuccessful())
                {
                    ArteVideoDTO video = gson.fromJson(responseVideoDetails.body().string(), ArteVideoDTO.class);

                    //The duration as time so it can be formatted and co.
                    LocalTime durationAsTime = durationAsTime(video.getDurationInSeconds());

                    if (video.getVideoUrls().containsKey(Qualities.NORMAL))
                    {
                        String broadcastBegin = ""; 

                        // TODO nicht wirklich gut!! teilweise keine Daten, teilweise "krumme" Datumswerte
                        // Stattdessen: links/programs/href aufrufen und broadcastBeginRounded nehmen
                        // TODO: Neu Alex Alles schöner machen!
//                        if(jsonObject.has(JSON_ELEMENT_BROADCAST)) {
//                            JsonElement jsonBegin = jsonObject.get(JSON_ELEMENT_BROADCAST);
//                            if(jsonBegin != JsonNull.INSTANCE) {
//                                broadcastBegin = jsonBegin.getAsString();    
//                            }
//                        }
                        
                        MVHttpClient mvhttpClient = MVHttpClient.getInstance();
                        OkHttpClient httpClient = mvhttpClient.getHttpClient();
                        Request request = new Request.Builder()
                                .addHeader(MediathekArte_de.AUTH_HEADER, MediathekArte_de.AUTH_TOKEN)
                                .url(videosUrlVideoDetails2).build();
                        
                        try(Response responseVideoDetails2 = httpClient.newCall(request).execute())
                        {
                          if(responseVideoDetails2.isSuccessful())
                          {
                            JsonObject jsonObjectVideoDetails2 = gson.fromJson(responseVideoDetails2.body().string(), JsonObject.class);
                            if(jsonObjectVideoDetails2.isJsonObject() && 
                                jsonObjectVideoDetails2.get("programs").getAsJsonArray().size() > 0 &&
                                jsonObjectVideoDetails2.get("programs").getAsJsonArray().get(0).getAsJsonObject()
                                  .get("broadcastProgrammings").getAsJsonArray().size() > 0 &&
                                jsonObjectVideoDetails2
                                  .get("programs").getAsJsonArray().get(0).getAsJsonObject()
                                  .get("broadcastProgrammings").getAsJsonArray().get(0).getAsJsonObject()
                                  .has(JSON_ELEMENT_BROADCAST)) {
                                JsonElement jsonBegin = jsonObjectVideoDetails2
                                    .get("programs").getAsJsonArray().get(0).getAsJsonObject()
                                    .get("broadcastProgrammings").getAsJsonArray().get(0).getAsJsonObject()
                                    .get(JSON_ELEMENT_BROADCAST);
                                if(jsonBegin != JsonNull.INSTANCE) {
                                    broadcastBegin = jsonBegin.getAsString();    
                                }
                            }
                          }
                        }

                        film = createFilm(thema, urlWeb, titel, video, broadcastBegin, durationAsTime, beschreibung);
                   } else {
                       LOG.debug(String.format("Keine \"normale\" Video URL für den Film \"%s\" mit der URL \"%s\". Video Details URL:\"%s\" ", titel, urlWeb, videosUrl));
                   }
                }
            } catch (IOException ioException)
            {
                LOG.error("Beim laden der Informationen eines Filmes für Arte kam es zu Verbindungsproblemen.", ioException);
            }
        }
        } catch(Exception e) {
            e.printStackTrace();
            LOG.error(e);
        }
        
        return film;
    }
    
    private static String getSubject(JsonObject programObject) {
        String subject;
        
        JsonObject catObject = programObject.get(JSON_ELEMENT_KEY_CATEGORY).getAsJsonObject();
        JsonObject subcatObject = programObject.get(JSON_ELEMENT_KEY_SUBCATEGORY).getAsJsonObject();

        String category = catObject != null ? getElementValue(catObject, JSON_ELEMENT_KEY_NAME) : "";
        String subcategory = subcatObject != null ? getElementValue(subcatObject, JSON_ELEMENT_KEY_NAME) : "";
        
        if(!category.equals(subcategory) && !subcategory.isEmpty()) {
            subject = category + " - " + subcategory;
        } else {
            subject = category;
        }

        return subject;
    }
    
    private static String getTitle(JsonObject programObject) {
        String title = getElementValue(programObject, JSON_ELEMENT_KEY_TITLE);
        String subtitle = getElementValue(programObject, JSON_ELEMENT_KEY_SUBTITLE);
                
        if (!title.equals(subtitle) && !subtitle.isEmpty()) {
            title = title + " - " + subtitle;
        }        
        
        return title;
    }

    private static boolean isValidProgramObject(JsonObject programObject) {
        return programObject.has(JSON_ELEMENT_KEY_TITLE) && 
            programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID) && 
            programObject.has(JSON_ELEMENT_KEY_URL) &&
            !programObject.get(JSON_ELEMENT_KEY_TITLE).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_URL).isJsonNull();
    }
    
    private static String getElementValue(JsonObject jsonObject, String elementName) {
        return !jsonObject.get(elementName).isJsonNull() ? jsonObject.get(elementName).getAsString() : "";        
    }
    
    private DatenFilm createFilm(String thema, String urlWeb, String titel, ArteVideoDTO video, String broadcastBegin, LocalTime durationAsTime, String beschreibung) {
        
        String date = MserverDatumZeit.formatDate(broadcastBegin, broadcastDate);
        String time = MserverDatumZeit.formatTime(broadcastBegin, broadcastDate);
        
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
        return film;
    }
    
    private Response executeRequest(String url) throws IOException {
        OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
        Request request = new Request.Builder().url(url).build();
                
        return httpClient.newCall(request).execute();   
    }
    
    private LocalTime durationAsTime(long aDurationInSeconds)
    {
        LocalTime localTime = LocalTime.MIN;
        
        localTime = localTime.plusSeconds(aDurationInSeconds);
        
        return localTime;
    }
}