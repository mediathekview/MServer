package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.CantCreateFilmException;
import mServer.crawler.CrawlerTool;

import java.time.LocalDateTime;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ArteJsonObjectToFilmCallable implements Callable<Film>
{
    private static final Logger LOG = LogManager.getLogger(ArteJsonObjectToFilmCallable.class);
    private static final String JSON_ELEMENT_KEY_CATEGORY = "category";
    private static final String JSON_ELEMENT_KEY_SUBCATEGORY = "subcategory";
    private static final String JSON_ELEMENT_KEY_NAME = "name";
    private static final String JSON_ELEMENT_KEY_TITLE = "title";
    private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_KEY_URL = "url";
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN_2 = "https://api.arte.tv/api/opa/v3/programs/%s/%s"; // Für broadcastBeginRounded
    private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";

    private final JsonObject jsonObject;
    private final String langCode;
    private final Sender sender;

    public ArteJsonObjectToFilmCallable(JsonObject aJsonObjec, String aLangCode, Sender aSender)
    {
        jsonObject = aJsonObjec;
        langCode = aLangCode;
        sender = aSender;
    }

    @Override
    public Film call() throws CantCreateFilmException
    {
        Film film = null;
        try {
            if(isValidProgramObject(jsonObject))
            {
                 String titel = getTitle(jsonObject);
            String thema = getSubject(jsonObject);

            String beschreibung = getElementValue(jsonObject, JSON_ELEMENT_KEY_SHORT_DESCRIPTION);

            String urlWeb = getElementValue(jsonObject, JSON_ELEMENT_KEY_URL);

    
                 //https://api.arte.tv/api/player/v1/config/[language:de/fr]/[programId]?platform=ARTE_NEXT
            String programId = getElementValue(jsonObject, JSON_ELEMENT_KEY_PROGRAM_ID);
            String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, langCode, programId);
    
    
                    Gson gson = new GsonBuilder().registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer()).create();
    
                    try (Response responseVideoDetails = executeRequest(videosUrl))
                    {
                        if (responseVideoDetails.isSuccessful())
                        {
                            ArteVideoDTO video = gson.fromJson(responseVideoDetails.body().string(), ArteVideoDTO.class);
    
                            //The duration as time so it can be formatted and co.
                            Duration duration = Duration.of(video.getDurationInSeconds(), ChronoUnit.SECONDS);
    
                            if (video.getVideoUrls().containsKey(Qualities.NORMAL))
                            {
                                ArteVideoDetailsDTO details = getVideoDetails(gson, programId);
                                
                                film = createFilm(thema, urlWeb, titel, video, details, duration, beschreibung);
                            } else {
                                LOG.debug(String.format("Keine \"normale\" Video URL für den Film \"%s\" mit der URL \"%s\". Video Details URL:\"%s\" ", titel, urlWeb, videosUrl));
                            }
                        }
                    } catch (IOException ioException)
                    {
                        LOG.fatal("Beim laden der Informationen eines Filmes für Arte kam es zu Verbindungsproblemen.", ioException);
                        throw new CantCreateFilmException(ioException);
                    }catch (URISyntaxException uriSyntaxEception)
                    {
                        throw new CantCreateFilmException(uriSyntaxEception);
                    }
                }
        } catch (Exception exception)
        {
            throw new CantCreateFilmException(exception);
        }
        return film;
    }
    
      private ArteVideoDetailsDTO getVideoDetails(Gson gson, String programId) throws IOException {
        ArteVideoDetailsDTO details = new ArteVideoDetailsDTO();
        
        OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
        //https://api.arte.tv/api/opa/v3/programs/[language:de/fr]/[programId]
        String videosUrlVideoDetails2 = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN_2, langCode, programId);
        Request request = new Request.Builder()
                .addHeader(MediathekArte_de.AUTH_HEADER, MediathekArte_de.AUTH_TOKEN)
                .url(videosUrlVideoDetails2).build();

        try(Response responseVideoDetails2 = httpClient.newCall(request).execute())
        {
          if(responseVideoDetails2.isSuccessful())
          {
            details = gson.fromJson(responseVideoDetails2.body().string(), ArteVideoDetailsDTO.class);
          }
        }      
        return details;
    }
    
    
    private String getSubject(JsonObject programObject) {
        String category = "";
        String subcategory = "";
        String subject;
        
        JsonElement catElement = programObject.get(JSON_ELEMENT_KEY_CATEGORY);
        if(!catElement.isJsonNull()) {
            JsonObject catObject = catElement.getAsJsonObject();
            category = catObject != null ? getElementValue(catObject, JSON_ELEMENT_KEY_NAME) : "";
        }
        
        JsonElement subcatElement = programObject.get(JSON_ELEMENT_KEY_SUBCATEGORY);
        if(!subcatElement.isJsonNull()) {
            JsonObject subcatObject = subcatElement.getAsJsonObject();
            subcategory = subcatObject != null ? getElementValue(subcatObject, JSON_ELEMENT_KEY_NAME) : "";
        }
       
        if(!category.equals(subcategory) && !subcategory.isEmpty()) {
            subject = category + " - " + subcategory;
        } else {
            subject = category;
        }

        return subject;
    }
    
    private String getTitle(JsonObject programObject) {
        String title = getElementValue(programObject, JSON_ELEMENT_KEY_TITLE);
        String subtitle = getElementValue(programObject, JSON_ELEMENT_KEY_SUBTITLE);
                
        if (!title.equals(subtitle) && !subtitle.isEmpty()) {
            title = title + " - " + subtitle;
        }        
        
        return title;
    }

    private boolean isValidProgramObject(JsonObject programObject)
    {
        return programObject.has(JSON_ELEMENT_KEY_TITLE) && 
            programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID) && 
            programObject.has(JSON_ELEMENT_KEY_URL) &&
            !programObject.get(JSON_ELEMENT_KEY_TITLE).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull() &&
            !programObject.get(JSON_ELEMENT_KEY_URL).isJsonNull();
    }

    private String getElementValue(JsonObject jsonObject, String elementName)
    {
        return !jsonObject.get(elementName).isJsonNull() ? jsonObject.get(elementName).getAsString() : "";        
    }

    private Film createFilm(final String aThema,
                            final String aUrlWeb,
                            final String aTitel,
                            final ArteVideoDTO aVideo,
                            final ArteVideoDetailsDTO aArteVideoDetails,
                            final Duration aDuration,
                            final String aBeschreibung) throws URISyntaxException
    {

        Collection<GeoLocations> geoLocations = CrawlerTool.getGeoLocations(sender,aVideo.getUrl(Qualities.NORMAL));
        if (aArteVideoDetails.getGeoLocation() != GeoLocations.GEO_NONE) {
            geoLocations.remove(GeoLocations.GEO_NONE);
            geoLocations.add(aArteVideoDetails.getGeoLocation());
        }

        Film film = new Film(UUID.randomUUID(), geoLocations, sender, aTitel, aThema, aArteVideoDetails.getBroadcastBegin(), aDuration, new URI(aUrlWeb));
        film.setBeschreibung(aBeschreibung);
        for(Qualities quality : aVideo.getVideoUrls().keySet())
        {
            film.addUrl(quality, CrawlerTool.stringToFilmUrl(aVideo.getUrl(quality)));
        }
        return film;
    }

    private Response executeRequest(String url) throws IOException
    {
        OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
        Request request = new Request.Builder().url(url).build();

        return httpClient.newCall(request).execute();
    }

}
