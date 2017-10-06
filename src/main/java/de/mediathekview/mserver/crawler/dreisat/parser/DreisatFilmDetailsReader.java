package de.mediathekview.mserver.crawler.dreisat.parser;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.parser.Parser;
import org.jsoup.select.Elements;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.zdf.json.DownloadDTO;
import de.mediathekview.mserver.crawler.zdf.json.ZDFDownloadDTODeserializer;
import mServer.crawler.CrawlerTool;

public class DreisatFilmDetailsReader {
  private static final String ELEMENT_BASENAME = "basename";
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm", Locale.GERMANY);
  private static final Logger LOG = LogManager.getLogger(DreisatFilmDetailsReader.class);
  private static final String ERROR_NO_START_TEMPLATE =
      "The 3Sat film \"%s - %s\" has no broadcast start so it will using the actual date and time.";
  private static final String ELEMENT_ORIGIN_CHANNEL_TITLE = "originChannelTitle";
  private static final String ELEMENT_ONLINEAIRTIME = "onlineairtime";
  private static final String ELEMENT_AIRTIME = "airtime";
  private static final String ELEMENT_GEOLOCATION = "geolocation";
  private static final String ELEMENT_LENGTH = "lengthSec";
  private static final String ELEMENT_DETAIL = "detail";
  private static final String ELEMENT_TITLE = "title";
  private static final String API_URL_PATTERN =
      "http://tmd.3sat.de/tmd/2/ngplayer_2_3/vod/ptmd/3sat/%s/2";
  private final URL xmlUrl;
  private final URL website;

  private final AbstractCrawler crawler;

  public DreisatFilmDetailsReader(final AbstractCrawler aCrawler, final URL aXmlUrl,
      final URL aWebsite) {
    crawler = aCrawler;
    xmlUrl = aXmlUrl;
    website = aWebsite;
  }

  public Optional<Film> readDetails() {
    final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    try (InputStream xmlStream = xmlUrl.openStream()) {
      final Document doc = Jsoup.connect(xmlUrl.toString()).parser(Parser.xmlParser()).get();
      final Elements titleNodes = doc.getElementsByTag(ELEMENT_TITLE);
      final Elements themaNodes = doc.getElementsByTag(ELEMENT_ORIGIN_CHANNEL_TITLE);

      final Elements descriptionNodes = doc.getElementsByTag(ELEMENT_DETAIL);
      final Elements durationNodes = doc.getElementsByTag(ELEMENT_LENGTH);
      final Elements geoLocNodes = doc.getElementsByTag(ELEMENT_GEOLOCATION);
      final Elements dateNodes = doc.getElementsByTag(ELEMENT_AIRTIME);
      final Elements alternativeDateNodes = doc.getElementsByTag(ELEMENT_ONLINEAIRTIME);
      final Elements filmUrlsApiUrlNodes = doc.getElementsByTag(ELEMENT_BASENAME);

      if (!titleNodes.isEmpty() && !themaNodes.isEmpty() && !durationNodes.isEmpty()
          && !filmUrlsApiUrlNodes.isEmpty()) {
        final String thema = themaNodes.get(0).text();
        final String title = titleNodes.get(0).text();

        GeoLocations geoLocation;
        if (!geoLocNodes.isEmpty()) {
          geoLocation = GeoLocations.getFromDescription(geoLocNodes.get(0).text());
        } else {
          geoLocation = GeoLocations.GEO_NONE;
        }
        final Collection<GeoLocations> geoLocations = new ArrayList<>();
        geoLocations.add(geoLocation);

        LocalDateTime time;
        if (!dateNodes.isEmpty() && dateNodes.get(0).text() != null) {
          time = LocalDateTime.parse(dateNodes.get(0).text(), DATE_TIME_FORMATTER);
        } else if (!alternativeDateNodes.isEmpty() && alternativeDateNodes.get(0).text() != null) {
          time = LocalDateTime.parse(alternativeDateNodes.get(0).text(), DATE_TIME_FORMATTER);
        } else {
          time = LocalDateTime.now();
          LOG.debug(String.format(ERROR_NO_START_TEMPLATE, thema, title));
        }

        final Duration dauer;
        if (durationNodes.get(0).text() != null) {
          dauer = Duration.ofSeconds(Integer.parseInt(durationNodes.get(0).text()));
        } else {
          dauer = Duration.ZERO;
        }

        final URL apiUrl =
            new URL(String.format(API_URL_PATTERN, filmUrlsApiUrlNodes.get(0).text()));
        final Gson gson = new GsonBuilder()
            .registerTypeAdapter(DownloadDTO.class, new ZDFDownloadDTODeserializer()).create();

        try (InputStreamReader gsonInputStreamReader = new InputStreamReader(apiUrl.openStream())) {
          final DownloadDTO downloadInfos = gson.fromJson(gsonInputStreamReader, DownloadDTO.class);
          geoLocations.add(downloadInfos.getGeoLocation());
          final Film newFilm = new Film(UUID.randomUUID(), geoLocations, Sender.DREISAT, title,
              thema, time, dauer, website);
          newFilm.addSubtitle(new URL(downloadInfos.getSubTitleUrl()));
          for (final Entry<Resolution, String> url : downloadInfos.getDownloadUrls().entrySet()) {
            newFilm.addUrl(url.getKey(), CrawlerTool.stringToFilmUrl(url.getValue()));
          }

          if (!descriptionNodes.isEmpty()) {
            newFilm.setBeschreibung(descriptionNodes.get(0).text());
          }
          return Optional.of(newFilm);
        }
      }


    } catch (final IOException exception) {
      LOG.fatal(String.format(
          "Something went teribble wrong on getting the film details for the 3Sat film \"%s\".",
          website.toString()), exception);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
    }
    return Optional.empty();
  }

}
