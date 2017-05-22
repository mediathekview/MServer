package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.CantCreateFilmException;
import mServer.crawler.CrawlerTool;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.UUID;
import java.util.concurrent.Callable;

public class ArteJsonObjectToFilmCallable implements Callable<Film>
{
    private static final Logger LOG = LogManager.getLogger(ArteJsonObjectToFilmCallable.class);
    private static final String JSON_OBJECT_KEY_PROGRAM = "program";
    private static final String JSON_ELEMENT_KEY_TITLE = "title";
    private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_KEY_URL = "url";
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s?platform=ARTE_NEXT";
    private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";
    private static final String JSON_ELEMENT_BROADCAST = "broadcastBeginRounded";

    private final JsonObject jsonObject;
    private final String langCode;
    private final Sender sender;

    private final DateTimeFormatter broadcastDateFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX");//2016-10-29T16:15:00Z

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
        if (jsonObject != null && jsonObject.has(JSON_OBJECT_KEY_PROGRAM))
        {
            JsonObject programObject = jsonObject.get(JSON_OBJECT_KEY_PROGRAM).getAsJsonObject();
            if (isValidProgramObject(programObject))
            {
                String thema = getElementValue(programObject, JSON_ELEMENT_KEY_TITLE);
                String titel = getElementValue(programObject, JSON_ELEMENT_KEY_SUBTITLE);
                String beschreibung = getElementValue(programObject, JSON_ELEMENT_KEY_SHORT_DESCRIPTION);

                String urlWeb = getElementValue(programObject, JSON_ELEMENT_KEY_URL);

                //https://api.arte.tv/api/player/v1/config/[language:de/fr]/[programId]
                String programId = getElementValue(programObject, JSON_ELEMENT_KEY_PROGRAM_ID);
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
                            String broadcastBegin = "";

                            if (jsonObject.has(JSON_ELEMENT_BROADCAST))
                            {
                                broadcastBegin = jsonObject.get(JSON_ELEMENT_BROADCAST).getAsString();
                            }

                            film = createFilm(thema, urlWeb, titel, video, LocalDateTime.parse(broadcastBegin, broadcastDateFormat), duration, beschreibung);
                        } else
                        {
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
        }
        return film;
    }

    private static boolean isValidProgramObject(JsonObject programObject)
    {
        return programObject.has(JSON_ELEMENT_KEY_TITLE) &&
                programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID) &&
                programObject.has(JSON_ELEMENT_KEY_URL) &&
                !programObject.get(JSON_ELEMENT_KEY_TITLE).isJsonNull() &&
                !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull() &&
                !programObject.get(JSON_ELEMENT_KEY_URL).isJsonNull();
    }

    private static String getElementValue(JsonObject jsonObject, String elementName)
    {
        return !jsonObject.get(elementName).isJsonNull() ? jsonObject.get(elementName).getAsString() : "";
    }

    private Film createFilm(final String aThema,
                            final String aUrlWeb,
                            final String aTitel,
                            final ArteVideoDTO aVideo,
                            final LocalDateTime aBroadcastBegin,
                            final Duration aDuration,
                            final String aBeschreibung) throws URISyntaxException
    {


        Film film = new Film(UUID.randomUUID(), CrawlerTool.getGeoLocations(sender,aVideo.getUrl(Qualities.NORMAL)), sender, aTitel, aThema, aBroadcastBegin, aDuration, new URI(aUrlWeb));
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

    private LocalTime durationAsTime(long aDurationInSeconds)
    {
        LocalTime localTime = LocalTime.MIN;

        localTime = localTime.plusSeconds(aDurationInSeconds);

        return localTime;
    }
}
