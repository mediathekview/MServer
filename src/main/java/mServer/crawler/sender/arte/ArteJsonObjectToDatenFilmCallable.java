package mServer.crawler.sender.arte;

import java.util.concurrent.Callable;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.DatenFilm;
import java.util.HashSet;
import java.util.Set;

public class ArteJsonObjectToDatenFilmCallable implements Callable<Set<DatenFilm>> {

  private static final Logger LOG = LogManager.getLogger(ArteJsonObjectToDatenFilmCallable.class);

  private static final String JSON_ELEMENT_KEY_PROGRAM_ID = "programId";

  private final JsonObject jsonObject;
  private final String langCode;
  private final String senderName;

  public ArteJsonObjectToDatenFilmCallable(JsonObject aJsonObjec, String aLangCode, String aSenderName) {
    jsonObject = aJsonObjec;
    langCode = aLangCode;
    senderName = aSenderName;
  }

  @Override
  public Set<DatenFilm> call() {
    Set<DatenFilm> films = new HashSet<>();
    try {
      if (isValidProgramObject(jsonObject)) {
        String programId = getElementValue(jsonObject, JSON_ELEMENT_KEY_PROGRAM_ID);
        films = new ArteProgramIdToDatenFilmCallable(programId, langCode, senderName).call();
      }
    } catch (Exception e) {
      e.printStackTrace();
      LOG.error(e);
    }

    return films;
  }

  private static String getElementValue(JsonObject jsonObject, String elementName) {
    return !jsonObject.get(elementName).isJsonNull() ? jsonObject.get(elementName).getAsString() : "";
  }

  private static boolean isValidProgramObject(JsonObject programObject) {
    return programObject.has(JSON_ELEMENT_KEY_PROGRAM_ID)
            && !programObject.get(JSON_ELEMENT_KEY_PROGRAM_ID).isJsonNull();
  }
}
