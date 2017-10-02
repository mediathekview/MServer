package de.mediathekview.mserver.crawler.dreisat.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;

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
  private static final String ELEMENT_LENGTH = "length";
  private static final String ELEMENT_DETAIL = "detail";
  private static final String ELEMENT_TITLE = "title";
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
      final DocumentBuilder builder = factory.newDocumentBuilder();
      final Document document = builder.parse(xmlStream);
      final NodeList titleNodes = document.getElementsByTagName(ELEMENT_TITLE);
      final NodeList themaNodes = document.getElementsByTagName(ELEMENT_ORIGIN_CHANNEL_TITLE);

      final NodeList descriptionNodes = document.getElementsByTagName(ELEMENT_DETAIL);
      final NodeList durationNodes = document.getElementsByTagName(ELEMENT_LENGTH);
      final NodeList geoLocNodes = document.getElementsByTagName(ELEMENT_GEOLOCATION);
      final NodeList dateNodes = document.getElementsByTagName(ELEMENT_AIRTIME);
      final NodeList alternativeDateNodes = document.getElementsByTagName(ELEMENT_ONLINEAIRTIME);
      final NodeList filmUrlsApiUrlNodes = document.getElementsByTagName(ELEMENT_BASENAME);

      if (titleNodes.getLength() > 0 && themaNodes.getLength() > 0 && durationNodes.getLength() > 0
          && filmUrlsApiUrlNodes.getLength() > 0) {
        final String thema = themaNodes.item(0).getNodeValue();
        final String title = titleNodes.item(0).getNodeValue();

        GeoLocations geoLocation;
        if (geoLocNodes.getLength() > 0) {
          geoLocation = GeoLocations.getFromDescription(geoLocNodes.item(0).getNodeValue());
        } else {
          geoLocation = GeoLocations.GEO_NONE;
        }
        final Collection<GeoLocations> geoLocations = new ArrayList<>();
        geoLocations.add(geoLocation);

        LocalDateTime time;
        if (dateNodes.getLength() > 0) {

          time = LocalDateTime.parse(dateNodes.item(0).getNodeValue(), DATE_TIME_FORMATTER);
        } else if (alternativeDateNodes.getLength() > 0) {
          time =
              LocalDateTime.parse(alternativeDateNodes.item(0).getNodeValue(), DATE_TIME_FORMATTER);
        } else {
          time = LocalDateTime.now();
          LOG.debug(String.format(ERROR_NO_START_TEMPLATE, thema, title));
        }

        final Duration dauer = Duration.ZERO;

        final Film newFilm = new Film(UUID.randomUUID(), geoLocations, Sender.DREISAT, title, thema,
            time, dauer, website);

        if (descriptionNodes.getLength() > 0) {
          newFilm.setBeschreibung(descriptionNodes.item(0).getNodeValue());
        }
        return Optional.of(newFilm);
      }


    } catch (SAXException | IOException | ParserConfigurationException exception) {
      LOG.fatal(String.format(
          "Something went teribble wrong on getting the film details for the 3Sat film \"%s\".",
          website.toString()), exception);
      crawler.incrementAndGetErrorCount();
      crawler.printErrorMessage();
    }
    return Optional.empty();
  }
}
