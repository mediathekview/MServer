package de.mediathekview.mserver.filmlisten.writer;

import static java.time.format.FormatStyle.MEDIUM;
import static java.time.format.FormatStyle.SHORT;

import com.google.gson.stream.JsonWriter;
import de.mediathekview.mserver.daten.AbstractMediaResource;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.MediaResourceComperators;
import de.mediathekview.mserver.daten.Podcast;
import de.mediathekview.mserver.daten.Resolution;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FilmlistOldFormatWriter extends AbstractFilmlistWriter {
  private static final Logger LOG = LogManager.getLogger(FilmlistOldFormatWriter.class);
  private static final DateTimeFormatter DATE_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(MEDIUM).withLocale(Locale.GERMANY);
  private static final DateTimeFormatter TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedTime(MEDIUM).withLocale(Locale.GERMANY);
  private static final String DURATION_FORMAT = "HH:mm:ss";
  private static final String URL_INTERSECTION_REDUCE_PATTERN = "%d|";
  private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
  private static final char GEO_SPLITTERATOR = '-';
  private static final DateTimeFormatter DATE_TIME_FORMAT =
      DateTimeFormatter.ofLocalizedDateTime(MEDIUM, SHORT).withLocale(Locale.GERMANY);

  private static final String META_HEADER_VERSION = "4";
  private static final String META_HEADER_VERSION_LONG = "MSearch [Vers.: 4.0.1]";

  private String sender = "";
  private String thema = "";

  @Override
  public boolean write(Filmlist filmlist, OutputStream outputStream) throws IOException {
    long start = System.currentTimeMillis();
    // these must be reset otherwise we may have old values in here
    this.sender = "";
    this.thema = "";
    AtomicInteger cnt = new AtomicInteger();
    try {
      LOG.info("start writting data");
      JsonWriter jsonWriter = new JsonWriter(new OutputStreamWriter(outputStream,StandardCharsets.UTF_8));
      jsonWriter.beginObject();
      writeMetaHeader(filmlist, jsonWriter);
      writeColumnHeader(jsonWriter);
      filmlist.getSorted(MediaResourceComperators.DEFAULT_COMPERATOR.getComparator()).forEach(aFilm -> {
        try {
          if (!aFilm.getUrls().isEmpty()) {
            writeRecord(aFilm, jsonWriter);
            cnt.getAndIncrement();
          }
          if (aFilm instanceof Film pFilm && !pFilm.getAudioDescriptions().isEmpty()) {
            Film filmAd = new Film(pFilm);
            if (!filmAd.getTitel().toLowerCase().contains("audiodeskription")) {
              filmAd.setTitel(filmAd.getTitel() + " (Audiodeskription)");
            }
            filmAd.setUrls(filmAd.getAudioDescriptions());
            writeRecord(filmAd, jsonWriter);
            cnt.getAndIncrement();
          }
          if (aFilm instanceof Film pFilm && !pFilm.getSignLanguages().isEmpty()) {
            Film filmGbs = new Film(pFilm);
            if (!filmGbs.getTitel().toLowerCase().contains("gebärdensprache")) {
              filmGbs.setTitel(filmGbs.getTitel() + " (Gebärdensprache)");
            }
            filmGbs.setUrls(filmGbs.getSignLanguages());
            writeRecord(filmGbs, jsonWriter);
            cnt.getAndIncrement();
          }
        } catch (IOException e) {
          LOG.error(e);
        }
      });
      jsonWriter.endObject();
      jsonWriter.flush();
      LOG.info("done writting in {} sec reading {} elements resulting in {} elements", ((System.currentTimeMillis()-start)/1000), cnt, filmlist.getFilms().size());
    } catch (IOException e) {
      LOG.error(e);
      return false;
    }
    return true;
  }

  private void writeMetaHeader(Filmlist list, JsonWriter jsonWriter ) throws IOException {
    jsonWriter.name("Filmliste").beginArray();
    jsonWriter.value(writeMetaHeader01CreationDate(list));
    jsonWriter.value(writeMetaHeader02CreationDateUTC(list));
    jsonWriter.value(META_HEADER_VERSION);
    jsonWriter.value(META_HEADER_VERSION_LONG);
    jsonWriter.value(writeMetaHeader05Id(list));
    jsonWriter.endArray();
    
  }
  
  private String writeMetaHeader01CreationDate(Filmlist in) {
    return DATE_TIME_FORMAT.format(in.getCreationDate());
  }

  private String writeMetaHeader02CreationDateUTC(Filmlist in) {
    return DATE_TIME_FORMAT.format(in.getCreationDate().atZone(ZoneOffset.UTC));    
  }

  private String writeMetaHeader05Id(Filmlist in) {
    return in.getListId().toString();
  }
  
  
  private void writeColumnHeader(JsonWriter jsonWriter) throws IOException {
    jsonWriter.name("Filmliste").beginArray();
    jsonWriter.value("Sender");
    jsonWriter.value("Thema");
    jsonWriter.value("Titel");
    jsonWriter.value("Datum");
    jsonWriter.value("Zeit");
    jsonWriter.value("Dauer");
    jsonWriter.value("Größe [MB]");
    jsonWriter.value("Beschreibung");
    jsonWriter.value("Url");
    jsonWriter.value("Website");
    jsonWriter.value("Url Untertitel");
    jsonWriter.value("Url RTMP");
    jsonWriter.value("Url Klein");
    jsonWriter.value("Url RTMP Klein");
    jsonWriter.value("Url HD");
    jsonWriter.value("Url RTMP HD");
    jsonWriter.value("DatumL");
    jsonWriter.value("Url History");
    jsonWriter.value("Geo");
    jsonWriter.value("neu");
    jsonWriter.endArray();
  }

  private void writeRecord(AbstractMediaResource<?> film, JsonWriter jsonWriter) throws IOException {
    jsonWriter.name("X").beginArray();
    jsonWriter.value(writeRecord01Sender(film, this.sender));
    jsonWriter.value(writeRecord02Thema(film, this.thema));
    jsonWriter.value(writeRecord03Titel(film));
    jsonWriter.value(writeRecord04Datum(film));
    jsonWriter.value(writeRecord05Zeit(film));
    jsonWriter.value(writeRecord06Dauer(film));
    jsonWriter.value(writeRecord07Groesse(film));
    jsonWriter.value(writeRecord08Beschreibung(film));
    jsonWriter.value(writeRecord09UrlNormal(film));
    jsonWriter.value(writeRecord10Website(film));
    jsonWriter.value(writeRecord11Untertitel(film));
    jsonWriter.value(""); // UrlRTMP
    jsonWriter.value(writeRecord13UrlKlein(film));
    jsonWriter.value(""); // UrlRTMP Klein
    jsonWriter.value(writeRecord15UrlHD(film));
    jsonWriter.value(""); // UrlRTMP HD
    jsonWriter.value(writeRecord17DatumL(film));
    jsonWriter.value(""); // Url History
    jsonWriter.value(writeRecord19Geo(film));
    jsonWriter.value(writeRecord20Neu(film));
    jsonWriter.endArray();
  }

  private String writeRecord01Sender(AbstractMediaResource<?> in, String aSender) {
    if (!aSender.equalsIgnoreCase(in.getSenderName())) {
      this.sender = in.getSenderName();
      return in.getSenderName();
    } else {
      return "";
    }
  }

  private String writeRecord02Thema(AbstractMediaResource<?> in, String aThema) {
    if(!aThema.equalsIgnoreCase(in.getThema()) ) {
      this.thema = in.getThema();
      return in.getThema();
    } else {
      return "";
    }
  }

  private String writeRecord03Titel(AbstractMediaResource<?> in) {
    return in.getTitel();
  }

  private String writeRecord04Datum(AbstractMediaResource<?> in) {
    return in.getTime().format(DATE_FORMATTER);
  }

  private String writeRecord05Zeit(AbstractMediaResource<?> in) {
    return in.getTime().format(TIME_FORMATTER);
  }
      
  private String writeRecord06Dauer(AbstractMediaResource<?> in) {
    if (!(in instanceof Podcast pIn) || pIn.getDuration().isZero()) {
      return "";
    }
    return LocalTime.MIDNIGHT.plus(pIn.getDuration()).format(DateTimeFormatter.ofPattern(DURATION_FORMAT));
  }

  private String writeRecord07Groesse(AbstractMediaResource<?> in) {
    if ((in instanceof Podcast pIn) && pIn.getUrl(Resolution.NORMAL) != null)
      return (pIn.getUrl(Resolution.NORMAL).getFileSize()/1024) + "";
    return "";
  }

  private String writeRecord08Beschreibung(AbstractMediaResource<?> in) {
    return in.getBeschreibung();
  }

  private String writeRecord09UrlNormal(AbstractMediaResource<?> in) {
    if ((in instanceof Podcast pIn) && pIn.getUrl(Resolution.NORMAL) != null) {
      return pIn.getUrl(Resolution.NORMAL).getUrl().toString();
    }
    return "";
  }

  private String writeRecord10Website(AbstractMediaResource<?> in) {
    final Optional<URL> website = in.getWebsite();
    if (website.isPresent()) {
      return website.get().toString();
    }
    return "";
  }

  private String writeRecord11Untertitel(AbstractMediaResource<?> in) {
    if ((in instanceof Film fIn) && !fIn.getSubtitles().isEmpty()) {
      return fIn.getSubtitles().toArray()[0].toString();
    }
    return "";
  }

  private String writeRecord13UrlKlein(AbstractMediaResource<?> in) {
    if ((in instanceof Podcast pIn) && in.getUrl(Resolution.SMALL) != null && pIn.getUrl(Resolution.NORMAL) != null) {
      return reduceUrl(pIn.getUrl(Resolution.NORMAL).getUrl().toString(), pIn.getUrl(Resolution.SMALL).getUrl().toString());
    }
    return "";
  }

  private String writeRecord15UrlHD(AbstractMediaResource<?> in) {
    if ((in instanceof Podcast pIn) && in.getUrl(Resolution.HD) != null && in.getUrl(Resolution.NORMAL) != null) {
      return reduceUrl(pIn.getUrl(Resolution.NORMAL).getUrl().toString(), pIn.getUrl(Resolution.HD).getUrl().toString());
    }
    return "";
  }

  private String writeRecord17DatumL(AbstractMediaResource<?> in) {
    final ZonedDateTime zonedDateTime = in.getTime().atZone(ZONE_ID);
    return zonedDateTime.toEpochSecond()+"";
  }

  private String writeRecord19Geo(AbstractMediaResource<?> in) {
    return geolocationsToStirng(in.getGeoLocations());
  }

  private String writeRecord20Neu(AbstractMediaResource<?> in) {
    if ((in instanceof Podcast pIn)) {
      return Boolean.toString(pIn.isNeu());
    }
    return Boolean.toString(false);
  }
  
  private String reduceUrl(final String aBaseUrl, final String aUrlToReduce) {
    final StringBuilder urlIntersectionBuilder = new StringBuilder();
    for (int i = 0;
        i < aBaseUrl.length()
            && i < aUrlToReduce.length()
            && aBaseUrl.charAt(i) == aUrlToReduce.charAt(i);
        i++) {
      urlIntersectionBuilder.append(aBaseUrl.charAt(i));
    }

    final String urlIntersection = urlIntersectionBuilder.toString();
    final String result;
    if (urlIntersection.isEmpty()) {
      result = aUrlToReduce;
    } else {
      result =
          aUrlToReduce.replace(
              urlIntersection,
              String.format(URL_INTERSECTION_REDUCE_PATTERN, urlIntersection.length()));
    }
    return result;
  }
  
  private String geolocationsToStirng(final Collection<GeoLocations> aGeoLocations) {
    final StringBuilder geolocationsStringBuilder = new StringBuilder();
    if (!aGeoLocations.isEmpty()) {
      for (final GeoLocations geoLocation : aGeoLocations) {
        geolocationsStringBuilder.append(geoLocation.getDescription());
        geolocationsStringBuilder.append(GEO_SPLITTERATOR);
      }
      geolocationsStringBuilder.deleteCharAt(
          geolocationsStringBuilder.lastIndexOf(String.valueOf(GEO_SPLITTERATOR)));
    }
    return geolocationsStringBuilder.toString();
  }
}
