package mServer.crawler.sender.arte;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;

public class ArteDatenFilmDeserializer implements JsonDeserializer<ListeFilme>
{
    private static final String JSON_ELEMENT_VIDEOS = "videos";
    private static final Logger LOG = LogManager.getLogger(ArteDatenFilmDeserializer.class);
    
    private final String langCode;
    private final String senderName;
    
    public ArteDatenFilmDeserializer(String aLangCode, String aSenderName) {
        langCode = aLangCode;
        senderName = aSenderName;
    }

    @Override
    public ListeFilme deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException
    {
        ListeFilme listeFilme = new ListeFilme();

        Collection<Future<DatenFilm>> futureFilme = new ArrayList<>();
        for (JsonElement jsonElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VIDEOS).getAsJsonArray())
        {
            ExecutorService executor = Executors.newCachedThreadPool();
            futureFilme.add(executor.submit(new ArteJsonObjectToDatenFilmCallable(jsonElement.getAsJsonObject(), langCode, senderName)));
        }
        
        CopyOnWriteArrayList<DatenFilm> finishedFilme = new CopyOnWriteArrayList<>();
        futureFilme.parallelStream().forEach(e -> {
            try{
                DatenFilm finishedFilm = e.get();
                if(finishedFilm!=null)
                {
                    finishedFilme.add(finishedFilm);
                }
            }catch(Exception exception)
            {
                LOG.error("Es ist ein Fehler beim lesen der Arte Filme aufgetreten.",exception);
            }

            });
        
        listeFilme.addAll(finishedFilme);
        return listeFilme;
    }
}
