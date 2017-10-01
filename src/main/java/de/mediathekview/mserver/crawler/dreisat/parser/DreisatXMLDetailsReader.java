package de.mediathekview.mserver.crawler.dreisat.parser;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import de.mediathekview.mlib.daten.GeoLocations;

public class DreisatXMLDetailsReader {
  private static final DateTimeFormatter DATE_TIME_FORMATTER =
      DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM).withLocale(Locale.GERMANY);
  private static final Logger LOG = LogManager.getLogger(DreisatXMLDetailsReader.class);
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

  public DreisatXMLDetailsReader(final URL aXmlUrl) {
    xmlUrl = aXmlUrl;
  }

  public void readDetails() {
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

      if (titleNodes.getLength() > 0 && themaNodes.getLength() > 0
          && durationNodes.getLength() > 0) {
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

        } else {
          time = LocalDateTime.now();
          LOG.debug(String.format(ERROR_NO_START_TEMPLATE, thema, title));
        }

        // final Film newFilm = new Film(UUID.randomUUID(), geoLocations, Sender.DREISAT, title,
        // thema,
        // aTime, aDauer, aWebsite);
      }


    } catch (SAXException | IOException | ParserConfigurationException exception) {
      // TODO Auto-generated catch block
      exception.printStackTrace();
    }
  }


}
