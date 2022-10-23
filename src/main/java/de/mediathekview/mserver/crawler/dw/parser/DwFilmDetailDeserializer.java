package de.mediathekview.mserver.crawler.dw.parser;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.FilmUrl;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.utils.JsonUtils;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.dw.DwVideoDto;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DwFilmDetailDeserializer
    implements JsonDeserializer<Optional<Film>> {
  private static final Logger LOG = LogManager.getLogger(DwFilmDetailDeserializer.class);

  private static final String ELEMENT_ID = "id";
  private static final String ELEMENT_TYPE = "type";
  private static final String ELEMENT_NAME = "name";
  private static final String ELEMENT_TEASER = "teaser";
  private static final String ELEMENT_CATEGORY = "categoryName";
  private static final String ELEMENT_LINK = "permaLink";
  private static final String ELEMENT_DATETIME = "displayDate";
  
  private static final String ELEMENT_MAINCONTENT = "mainContent";
  private static final String ELEMENT_MAINCONTENT_LINK = "url";
  private static final String ELEMENT_MAINCONTENT_DURATION = "duration";
  private static final String ELEMENT_MAINCONTENT_SOURCES = "sources";
  private static final String ELEMENT_MAINCONTENT_SOURCES_QUALITY = "quality";
  private static final String ELEMENT_MAINCONTENT_SOURCES_URL = "url";
  private static final String ELEMENT_MAINCONTENT_SOURCES_FORMAT = "format";
  private static final String ELEMENT_MAINCONTENT_SOURCES_BITRATE = "bitrate";

  private final Sender sender;
  private final AbstractCrawler crawler;

  public DwFilmDetailDeserializer(final AbstractCrawler aCrawler) {
    this.sender = aCrawler.getSender();
    crawler = aCrawler;
  }

  @Override
  public Optional<Film> deserialize(
      JsonElement aJsonElement, Type aType, JsonDeserializationContext aContext) {

    final JsonObject jsonObject = aJsonElement.getAsJsonObject();

    final Optional<String> videoId = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_ID);
    final Optional<String> type = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_TYPE);
    final Optional<String> title = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_NAME);
    final Optional<String> topic = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_CATEGORY);

    if (!topic.isPresent() || !title.isPresent() || !videoId.isPresent() || !type.isPresent()) {
      LOG.error("Could not find mandatory element title {} or topic {} or videoid {} or type {}", title.orElse(""), topic.orElse(""),videoId.orElse(""),type.orElse(""));
      return Optional.empty();
    }
    if (!type.get().equalsIgnoreCase("video")) {
    	//LOG.error("element is not a video {} but {}", videoId.orElse(title.orElse(topic.orElse(""))), type.orElse(""));
        return Optional.empty();    	
    }
    
    if (!jsonObject.has(ELEMENT_MAINCONTENT)) {
      LOG.error("Could not find maincontent for video {}", videoId.get());
      return Optional.empty();
    }

    final JsonObject jsonObjectMainContent = jsonObject.get(ELEMENT_MAINCONTENT).getAsJsonObject();
    final Optional<String> thisPageUrl = JsonUtils.getAttributeAsString(jsonObjectMainContent, ELEMENT_MAINCONTENT_LINK);
    
    if (!jsonObjectMainContent.has(ELEMENT_MAINCONTENT_SOURCES)) {
      LOG.error("Could not find sources for video {} url {}", videoId.get(), thisPageUrl.orElse(""));
      return Optional.empty();
    }
    
    final Film film =
        new Film(UUID.randomUUID(), sender, title.get(), topic.get(), getAiredDate(thisPageUrl.get(),jsonObject), getDuration(thisPageUrl.get(),jsonObjectMainContent));
    //
    final Optional<String> description = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_TEASER);
    description.ifPresent(c -> film.setBeschreibung(c));
    //
    getWebsite(thisPageUrl.get(), jsonObject).ifPresent(c -> film.setWebsite(c));
    //
    final JsonArray jsonObjectMainContentSources = jsonObjectMainContent.get(ELEMENT_MAINCONTENT_SOURCES).getAsJsonArray();
    getVideos(title.get(),jsonObjectMainContentSources).ifPresent(c -> film.addAllUrls(c));
    //
    return Optional.of(film);
  }
  
  private Optional<URL> getWebsite(final String videoid, final JsonObject jsonObject) {
    Optional<URL> websiteUrl = Optional.empty();
    final Optional<String> websiteString = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_LINK);
    if (websiteString.isPresent()) {
      try {
        websiteUrl = Optional.of(new URL(websiteString.get()));
      } catch (Exception e) {
        LOG.error("Error getWebsite for video {} on value '{}'", videoid, websiteString.get());
      }
    } else {
      LOG.error("no error getWebsite found for video {}" + videoid);
    }
    return websiteUrl;
  }
  
  private LocalDateTime getAiredDate(final String videoid, final JsonObject jsonObject) {
    final DateTimeFormatter DATE_FORMATTER =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX");
    LocalDateTime airedDatetime = LocalDateTime.now();
    final Optional<String> displayDate = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_DATETIME);
    if (displayDate.isPresent()) {
      try {
        airedDatetime = LocalDateTime.parse(displayDate.get(), DATE_FORMATTER);
      } catch (Exception e) {
        LOG.error("error getAiredDate for video {} on value '{}'", videoid, displayDate.get());
      }
    } else {
      LOG.error("no error airedDate found for video {}" + videoid);
    }
    return airedDatetime;
  }
  
  private Duration getDuration(final String videoid, final JsonObject jsonObject) {
    Duration duration = Duration.ofSeconds(0);
    final Optional<String> durationString = JsonUtils.getAttributeAsString(jsonObject, ELEMENT_MAINCONTENT_DURATION);
    if (durationString.isPresent()) {
      try {
        duration = Duration.ofSeconds(Integer.parseInt(JsonUtils.getAttributeAsString(jsonObject, ELEMENT_MAINCONTENT_DURATION).get()));
      } catch (Exception e) {
        LOG.error("error getDuration for video {} on value '{}'", videoid, durationString.get());
      }
    } else {
      LOG.error("no error duration found for video {}" + videoid);
    }
    return duration;
  }

  private Optional<Map<Resolution, FilmUrl>> getVideos(final String videoid, final JsonArray videos) {

    if (videos == null) {
      return Optional.empty();
    }

    final Map<Resolution, FilmUrl> videoListe = new ConcurrentHashMap<>();
    final ArrayList<DwVideoDto> videoListeRaw = new ArrayList<DwVideoDto>();

    videos.forEach(
      (JsonElement currentElement) -> {
        if (currentElement.isJsonObject()) {
          final JsonObject currentElementObject = currentElement.getAsJsonObject();
          final Optional<String> quality = JsonUtils.getAttributeAsString(currentElementObject, ELEMENT_MAINCONTENT_SOURCES_QUALITY);
          final Optional<String> bitrate = JsonUtils.getAttributeAsString(currentElementObject, ELEMENT_MAINCONTENT_SOURCES_BITRATE);
          final Optional<String> format = JsonUtils.getAttributeAsString(currentElementObject, ELEMENT_MAINCONTENT_SOURCES_FORMAT);
          final Optional<String> url = JsonUtils.getAttributeAsString(currentElementObject, ELEMENT_MAINCONTENT_SOURCES_URL);

          if (url.isPresent() && quality.isPresent() && format.isPresent() && bitrate.isPresent()) {
            try {
              if (!format.get().equalsIgnoreCase("hls")) {
                videoListeRaw.add(new DwVideoDto(Integer.parseInt(bitrate.get()), quality.get(), format.get(), new URL(url.get())));
              }
            } catch (final MalformedURLException e) {
              // Nothing to be done here
              LOG.error("Malformed video url for video: {}", videoid);
            }
          } else {
            LOG.error("Mising video url element for video: {}", videoid);
          }
        }
      }
    );
    //
    for (int quality = 0; quality < videoListeRaw.size(); quality++) {
      final FilmUrl filmUrl = new FilmUrl(videoListeRaw.get(quality).getUrl(), crawler.determineFileSizeInKB(videoListeRaw.get(quality).getUrl().toExternalForm()));
      videoListe.put(getResolutionFromPosition(quality), filmUrl);
    }
    //
    if (videoListe.size() > 0) {
      return Optional.of(videoListe);
    }
    LOG.error("No video url for video: {}", videoid);
    return Optional.empty();
  }
  
  private Resolution getResolutionFromPosition(int pos) {
	switch (pos) {
	  case 0: {
		return Resolution.VERY_SMALL;
	  }
	  case 1: {
	    return Resolution.SMALL;
	  }
	  case 2: {
        return Resolution.NORMAL;
      }
	  case 3: {
	    return Resolution.HD;
	  }
	  default:
		return Resolution.UHD;
	}
  }
}
