package mServer.crawler.sender.arte;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import java.util.Set;

public class ArteDatenFilmDeserializer implements JsonDeserializer<ListeFilme> {

  private static final String JSON_ELEMENT_VIDEOS = "videos";

  private final String langCode;
  private final String senderName;

  public ArteDatenFilmDeserializer(String aLangCode, String aSenderName) {
    langCode = aLangCode;
    senderName = aSenderName;
  }

  @Override
  public ListeFilme deserialize(JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) throws JsonParseException {
    ListeFilme listeFilme = new ListeFilme();

    Collection<DatenFilm> futureFilme = new ArrayList<>();
    for (JsonElement jsonElement : aJsonElement.getAsJsonObject().get(JSON_ELEMENT_VIDEOS).getAsJsonArray()) {
      Set<DatenFilm> films = new ArteJsonObjectToDatenFilmCallable(jsonElement.getAsJsonObject(), langCode, senderName).call();
      for (DatenFilm film : films) {
        futureFilme.add(film);
      }
    }

    final List<DatenFilm> list = futureFilme.parallelStream()
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    listeFilme.addAll(list);
    list.clear();

    return listeFilme;
  }
}
