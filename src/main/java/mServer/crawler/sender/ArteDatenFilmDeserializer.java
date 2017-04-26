package mServer.crawler.sender;

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

public class ArteDatenFilmDeserializer implements JsonDeserializer<ListeFilme> {
    
    @Override
    public ListeFilme deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
       ListeFilme listeFilme = new ListeFilme();
       
       for(JsonElement jsonElement:aJsonElement.getAsJsonArray())
       {
           DatenFilm datenFilm = elementToFilm(jsonElement.getAsJsonObject());
           if(null!=datenFilm){
               listeFilme.add(datenFilm);
           }

       }
       
        return listeFilme;
    }
    
    private DatenFilm elementToFilm(JsonObject aJsonObject)
    {
        JsonObject programObject = aJsonObject.get("program").getAsJsonObject();
        
        String thema = programObject.get("title").getAsString();
        String titel = programObject.get("subtitle").getAsString();
        String urlWeb = programObject.get("url").getAsString();
        
        //https://api.arte.tv/api/player/v1/config/[language:de/fr]/[programId]
        String programId = programObject.get("programId").getAsString();
        String videosUrl = String.format("https://api.arte.tv/api/player/v1/config/%s/%s","de",programId);
        
        Gson gson = new GsonBuilder().registerTypeAdapter(ArteVideoDTO.class, new ArteVideoDeserializer()).create();
        
        OkHttpClient httpClient = MVHttpClient.getInstance().getHttpClient();
        Request request = new Request.Builder().url(videosUrl).build();
        Response response = httpClient.newCall(request).execute();
        
        ArteVideoDTO video = gson.fromJson(response,ArteVideoDTO.class);
        
        DatenFilm film = new DatenFilm(Const.ARTE_DE, thema, urlWeb, titel, video.getUrl(Qualities.NORMAL), "" /*urlRtmp*/,
                                            datum, "" /*zeit*/, duration, beschreibung);
                                    if (video.getVideoUrls().containsKey(Qualities.HD)) {
                                        CrawlerTool.addUrlHd(film, video.getUrl(Qualities.HD), "");
                                    }
                                    if (video.getVideoUrls().containsKey(Qualities.SMALL)) {
                                        CrawlerTool.addUrlKlein(film, video.getUrl(Qualities.SMALL), "");
                                    }
    }
}
