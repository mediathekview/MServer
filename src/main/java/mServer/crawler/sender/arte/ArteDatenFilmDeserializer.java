package mServer.crawler.sender.arte;

import java.io.IOException;
import java.lang.reflect.Type;

import mServer.crawler.CrawlerTool;
import mServer.crawler.sender.newsearch.Qualities;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.MVHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ArteDatenFilmDeserializer implements JsonDeserializer<ListeFilme>
{
    private static final Logger LOG = LogManager.getLogger(ArteDatenFilmDeserializer.class);
    private static final String JSON_OBJECT_KEY_PROGRAM = "program";
    private static final String JSON_ELEMENT_KEY_TITLE = "title";
    private static final String JSON_ELEMENT_KEY_SUBTITLE = "subtitle";
    private static final String JSON_ELEMENT_KEY_URL = "url";
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";
    private static final String ARTE_VIDEO_INFORMATION_URL_PATTERN = "https://api.arte.tv/api/player/v1/config/%s/%s";
    private static final String ARTE_VIDEO_INFORMATION_URL_COUNTRY_GERMANY = "de";
    private static final String JSON_ELEMENT_KEY_SHORT_DESCRIPTION = "shortDescription";

    @Override
    public ListeFilme deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException
    {
        ListeFilme listeFilme = new ListeFilme();

        for (JsonElement jsonElement : aJsonElement.getAsJsonArray())
        {
            DatenFilm datenFilm = elementToFilm(jsonElement.getAsJsonObject());
            if (null != datenFilm)
            {
                listeFilme.add(datenFilm);
            }

        }

        return listeFilme;
    }

    private DatenFilm elementToFilm(JsonObject aJsonObject)
    {
        JsonObject programObject = aJsonObject.get(JSON_OBJECT_KEY_PROGRAM).getAsJsonObject();

        String thema = programObject.get(JSON_ELEMENT_KEY_TITLE).getAsString();
        String titel = programObject.get(JSON_ELEMENT_KEY_SUBTITLE).getAsString();
        String urlWeb = programObject.get(JSON_ELEMENT_KEY_URL).getAsString();
        String beschreibung = programObject.get(JSON_ELEMENT_KEY_SHORT_DESCRIPTION).getAsString();

        //https://api.arte.tv/api/player/v1/config/[language:de/fr]/[programId]
        String programId = programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).getAsString();
        String videosUrl = String.format(ARTE_VIDEO_INFORMATION_URL_PATTERN, ARTE_VIDEO_INFORMATION_URL_COUNTRY_GERMANY, programId);

        Gson gson = new GsonBuilder().registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer()).create();

        OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
        Request request = new Request.Builder().url(videosUrl).build();

        try
        {
            Response response = httpClient.newCall(request).execute();

            ArteVideoDTO video = gson.fromJson(response.body().string(), ArteVideoDTO.class);

            DatenFilm film = new DatenFilm(Const.ARTE_DE, thema, urlWeb, titel, video.getUrl(Qualities.NORMAL), "" /*urlRtmp*/,
                    datum, "" /*zeit*/, duration, beschreibung);
            if (video.getVideoUrls().containsKey(Qualities.HD))
            {
                CrawlerTool.addUrlHd(film, video.getUrl(Qualities.HD), "");
            }
            if (video.getVideoUrls().containsKey(Qualities.SMALL))
            {
                CrawlerTool.addUrlKlein(film, video.getUrl(Qualities.SMALL), "");
            }
        } catch (IOException ioException)
        {
            LOG.error("Beim laden der Informationen eines Filmes für Arte kam es zu Verbindungsproblemen.", ioException);
        }
    }
}
