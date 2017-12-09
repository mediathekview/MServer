package mServer.crawler.sender.arte;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;

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

        Collection<DatenFilm> futureFilme = new ArrayList<>();
        for (JsonElement jsonElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VIDEOS).getAsJsonArray())
        {
            futureFilme.add(new ArteJsonObjectToDatenFilmCallable(jsonElement.getAsJsonObject(), langCode, senderName).call());
        }
        
        CopyOnWriteArrayList<DatenFilm> finishedFilme = new CopyOnWriteArrayList<>();
        futureFilme.parallelStream().forEach(finishedFilm -> {
            try{
                if (finishedFilm != null)
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
