package de.mediathekview.mserver.crawler.funk.parser;

import static de.mediathekview.mserver.base.utils.JsonUtils.hasElements;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.funk.tasks.FunkSendungDTO;
import mServer.crawler.CrawlerTool;

public class FunkFilmDeserializer implements JsonDeserializer<Optional<Film>> {
  private static final String TIME_BLOCK_SPLITTERATOR = ":";
  private static final String JSON_ELEMENT_ATTRIBUTES = "attributes";
  private static final String JSON_ELEMENT_SHARING_URL = "sharingUrl";
  private static final String JSON_ELEMENT_SHORT_DESCRIPTION = "shortDescription";
  private static final String ERROR_TEXT_INVALID_URL_PATTERN = "The URL \"%s\" isn't a valid URL.";
  private static final String JSON_ELEMENT_DOWNLOAD_URL = "downloadUrl";
  private static final String JSON_ELEMENT_DURATION = "duration";
  private static final String JSON_ELEMENT_PUBLICATION_DATE = "publicationDate";
  private static final Logger LOG = LogManager.getLogger(FunkFilmDeserializer.class);
  private static final String JSON_ELEMENT_TITLE = "title";

  private static final String JSON_ELEMENT_DATA = "data";

  private final FunkSendungDTO sendungDTO;
  private final AbstractCrawler crawler;

  public FunkFilmDeserializer(final FunkSendungDTO aSendungDTO, final AbstractCrawler aCrawler) {
    sendungDTO = aSendungDTO;
    crawler = aCrawler;
  }

  @Override
  public Optional<Film> deserialize(final JsonElement aJsonElement, final Type aType,
      final JsonDeserializationContext aJsonDeserializationContext) {
    if (hasElements(aJsonElement, Optional.of(crawler), JSON_ELEMENT_DATA)
        && aJsonElement.getAsJsonObject().get(JSON_ELEMENT_DATA).getAsJsonArray().size() > 0
        && aJsonElement.getAsJsonObject().get(JSON_ELEMENT_DATA).getAsJsonArray().get(0)
            .isJsonObject()) {
      final JsonObject dataObj = aJsonElement.getAsJsonObject().get(JSON_ELEMENT_DATA)
          .getAsJsonArray().get(0).getAsJsonObject();
      if (hasElements(dataObj, Optional.of(crawler), JSON_ELEMENT_ATTRIBUTES)
          && dataObj.get(JSON_ELEMENT_ATTRIBUTES).isJsonObject()) {
        final JsonObject attributeObj = dataObj.get(JSON_ELEMENT_ATTRIBUTES).getAsJsonObject();
        return processAttribute(attributeObj);
      }
    }
    return Optional.empty();
  }

  private Duration loadDuration(final long aAsLong) {
    return Duration.of(aAsLong, ChronoUnit.SECONDS);
  }

  private String loadThema(final String titel) {
    final Optional<String> thema = sendungDTO.getThema();
    if (thema.isPresent()) {
      return thema.get();
    } else {
      LOG.debug(String.format("Don't know the Thema for video \"%s\" with the URL: \"%s\"", titel,
          sendungDTO.getUrl()));
      return "";
    }
  }

  private LocalDateTime loadTime(final String aIsoDateTimeText) {
    final StringBuilder isoDateBuilder =
        new StringBuilder(aIsoDateTimeText.substring(0, aIsoDateTimeText.length() - 2));
    isoDateBuilder.append(TIME_BLOCK_SPLITTERATOR);
    isoDateBuilder.append(aIsoDateTimeText.substring(aIsoDateTimeText.length() - 2));
    return LocalDateTime.parse(isoDateBuilder.toString(), DateTimeFormatter.ISO_DATE_TIME);
  }

  private Optional<Film> processAttribute(final JsonObject attributeObj) {
    if (hasElements(attributeObj, Optional.of(crawler), JSON_ELEMENT_TITLE,
        JSON_ELEMENT_PUBLICATION_DATE, JSON_ELEMENT_DURATION, JSON_ELEMENT_DOWNLOAD_URL,
        JSON_ELEMENT_SHARING_URL)) {
      final String titel = attributeObj.get(JSON_ELEMENT_TITLE).getAsString();
      final String thema = loadThema(titel);
      final String website = attributeObj.get(JSON_ELEMENT_SHARING_URL).getAsString();
      final LocalDateTime time =
          loadTime(attributeObj.get(JSON_ELEMENT_PUBLICATION_DATE).getAsString());
      final Duration dauer = loadDuration(attributeObj.get(JSON_ELEMENT_DURATION).getAsLong());

      try {
        final Film newFilm = new Film(UUID.randomUUID(), new ArrayList<>(), Sender.FUNK, titel,
            thema, time, dauer, new URL(website));

        final String downloadUrl = attributeObj.get(JSON_ELEMENT_DOWNLOAD_URL).getAsString();
        if (setDownloadUrl(newFilm, downloadUrl)) {
          if (hasElements(attributeObj, JSON_ELEMENT_SHORT_DESCRIPTION)) {
            newFilm.setBeschreibung(attributeObj.get(JSON_ELEMENT_SHORT_DESCRIPTION).getAsString());
          }

          return Optional.of(newFilm);
        }
      } catch (final MalformedURLException malformedURLException) {
        LOG.fatal(String.format(ERROR_TEXT_INVALID_URL_PATTERN, website), malformedURLException);
        crawler.printInvalidUrlErrorMessage(sendungDTO.getUrl());
      }

    }
    return Optional.empty();
  }

  private boolean setDownloadUrl(final Film newFilm, final String downloadUrl) {
    try {
      newFilm.addUrl(Resolution.NORMAL, CrawlerTool.stringToFilmUrl(downloadUrl));
      return true;
    } catch (final MalformedURLException malformedURLException) {
      LOG.fatal(String.format(ERROR_TEXT_INVALID_URL_PATTERN, sendungDTO.getUrl()),
          malformedURLException);
      crawler.printInvalidUrlErrorMessage(downloadUrl);
    }
    return false;
  }

}
