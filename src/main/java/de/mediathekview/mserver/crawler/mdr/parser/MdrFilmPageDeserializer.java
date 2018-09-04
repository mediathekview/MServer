package de.mediathekview.mserver.crawler.mdr.parser;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class MdrFilmPageDeserializer {

  private static final String FILM_ENTRY_SELECTOR = "div.sectionDetailPage div.mediaCon";
  private static final String ATTRIBUTE_DATA_CTRL_PLAYER = "data-ctrl-player";
  private static final Type TYPE_STRING_OPTIONAL = new TypeToken<Optional<String>>() {
  }.getType();

  private final Gson gson;

  private final String baseUrl;

  public MdrFilmPageDeserializer(final String aBaseUrl) {

    baseUrl = aBaseUrl;
    gson = new GsonBuilder()
        .registerTypeAdapter(TYPE_STRING_OPTIONAL, new MdrFilmPlayerJsonDeserializer())
        .create();
  }

  public Set<CrawlerUrlDTO> deserialize(final Document aDocument) {
    Set<CrawlerUrlDTO> filmEntries = new HashSet<>();

    Elements entryElements = aDocument.select(FILM_ENTRY_SELECTOR);
    for (Element entryElement : entryElements) {
      if (entryElement.hasAttr(ATTRIBUTE_DATA_CTRL_PLAYER)) {
        addFilmUrl(filmEntries, entryElement.attr(ATTRIBUTE_DATA_CTRL_PLAYER));
      }
    }

    return filmEntries;
  }

  private void addFilmUrl(final Set<CrawlerUrlDTO> aFilmEntries, final String attr) {

    Optional<String> entryUrl = gson.fromJson(attr, TYPE_STRING_OPTIONAL);

    entryUrl.ifPresent(s -> aFilmEntries.add(new CrawlerUrlDTO(baseUrl + s)));
  }
}
