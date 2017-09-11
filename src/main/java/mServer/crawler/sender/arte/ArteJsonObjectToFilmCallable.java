package mServer.crawler.sender.arte;

import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import mServer.crawler.CantCreateFilmException;

import java.util.concurrent.Callable;

public class ArteJsonObjectToFilmCallable implements Callable<Film>
{
    private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";

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
                String programId = getElementValue(jsonObject, JSON_ELEMENT_KEY_PROGRAM_ID);
                film = new ArteProgramIdToDatenFilmCallable(programId, langCode, sender).call();
            }
        } catch(Exception exception) {
            throw new CantCreateFilmException(exception);

        }
        return film;
    }
    
    private static String getElementValue(JsonObject jsonObject, String elementName) {
        return !jsonObject.get(elementName).isJsonNull() ? jsonObject.get(elementName).getAsString() : "";        
    }
    
    private static boolean isValidProgramObject(JsonObject programObject) {
        return programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID) && 
            !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull();

    }
}
