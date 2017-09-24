package mServer.crawler.sender.arte;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Sender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.*;

public class ArteFilmDeserializer implements JsonDeserializer<ListeFilme>
{
    private static final String JSON_ELEMENT_VIDEOS = "videos";
    private static final Logger LOG = LogManager.getLogger(ArteFilmDeserializer.class);
    
    private final String langCode;
    private final Sender sender;
    
    public ArteFilmDeserializer(String aLangCode, Sender aSender) {
        langCode = aLangCode;
        sender = aSender;
    }

    @Override
    public ListeFilme deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException
    {
        ListeFilme listeFilme = new ListeFilme();

        Collection<Future<Film>> futureFilme = new ArrayList<>();
        
        for (JsonElement jsonElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VIDEOS).getAsJsonArray())
        {
            ExecutorService executor = Executors.newCachedThreadPool();
            futureFilme.add(executor.submit(new ArteJsonObjectToFilmCallable(jsonElement.getAsJsonObject(), langCode, sender)));
        }
        
        CopyOnWriteArrayList<Film> finishedFilme = new CopyOnWriteArrayList<>();
        futureFilme.parallelStream().forEach(e -> {
            try{
                Film finishedFilm = e.get();
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
