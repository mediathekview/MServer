package de.mediathekview.mserver.crawler.mdr.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mserver.testhelper.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.xml.sax.SAXException;

@RunWith(Parameterized.class)
public class MdrFilmXmlHandlerTest {

  private String xmlFile;
  private String expectedTopic;
  private String expectedTitle;
  private String expectedDescription;
  private LocalDateTime expectedTime;
  private Duration expectedDuration;
  private String expectedWebsite;
  private String expectedUrlSmall;
  private String expectedUrlNormal;
  private String expectedUrlHd;
  private String expectedSubtitle;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/mdr/mdr_film_info.xml",
                "Musik & Show",
                "Ein Abend für Marianne Kiefer",
                "Als Paula Zipfel und Olga Knopf wurde sie berühmt: Marianne Kiefer. In vielen Lustspielen war die nur 1,54 große Künstlerin die Partnerin von Publikumslieblingen wie Herbert Köfer, Heinz Rennhack oder Ingeborg Krabbe.",
                LocalDateTime.of(2018, 9, 2, 20, 15, 0),
                Duration.ofHours(1).plusMinutes(29).plusSeconds(29),
                "https://www.mdr.de/mediathek/fernsehen/video-226902_zc-7748e51b_zs-1638fa4e.html",
                "https://odmdr-a.akamaihd.net/mp4dyn2/1/FCMS-1d15c65d-aca1-4c75-88a6-fd6589ad3741-9a4bb04739be_1d.mp4",
                "https://odmdr-a.akamaihd.net/mp4dyn2/1/FCMS-1d15c65d-aca1-4c75-88a6-fd6589ad3741-730aae549c28_1d.mp4",
                "https://odmdr-a.akamaihd.net/mp4dyn2/1/FCMS-1d15c65d-aca1-4c75-88a6-fd6589ad3741-be7c2950aac6_1d.mp4",
                "https://www.mdr.de/mediathek/mdr-videos/b/video-226902-videoSubtitle.xml"
            },
            {
              "/mdr/mdr_film_info_no_subtitle_geo.xml",
                "Alles Klara",
                "Alles Klara (45, mit Audiodeskription)",
                "Tierarzt Dr. Bramme wird an einem Baum tot aufgefunden. Er wurde beim Wasserlassen am Straßenrand kaltblütig erschossen. Die Kripo Harz beginnt zu ermitteln, allen voran Sekretärin Klara Degen. (nur in D abrufbar)",
                LocalDateTime.of(2018,9,1,10,5,0),
                Duration.ofMinutes(48).plusSeconds(4),
                "https://www.mdr.de/mediathek/fernsehen/a-z/video-225808_zc-ca8ec3f4_zs-73445a6d.html",
                "https://odgeomdr-a.akamaihd.net/mp4dyn2/2/FCMS-2873f3b3-e466-4a0c-999d-f9d1283d5481-9a4bb04739be_28.mp4",
                "https://odgeomdr-a.akamaihd.net/mp4dyn2/2/FCMS-2873f3b3-e466-4a0c-999d-f9d1283d5481-730aae549c28_28.mp4",
                "https://odgeomdr-a.akamaihd.net/mp4dyn2/2/FCMS-2873f3b3-e466-4a0c-999d-f9d1283d5481-be7c2950aac6_28.mp4",
                null
            }
        });
  }

  public MdrFilmXmlHandlerTest(final String aXmlFile, final String aExpectedTopic, final String aExpectedTitle,
      final String aExpectedDescription,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration, final String aExpectedWebsite,
      final String aExpectedUrlSmall, final String aExpectedUrlNormal,
      final String aExpectedUrlHd, final String aExpectedSubtitle) {

    xmlFile = aXmlFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
  }

  @Test
  public void test() throws URISyntaxException, ParserConfigurationException, SAXException, IOException {
    final URI uri = FileReader.class.getResource(xmlFile).toURI();

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    MdrFilmXmlHandler handler = new MdrFilmXmlHandler();
    saxParser.parse(uri.toString(), handler);

    assertThat(handler.getTopic(), equalTo(expectedTopic));
    assertThat(handler.getTitle(), equalTo(expectedTitle));
    assertThat(handler.getDescription(), equalTo(expectedDescription));
    assertThat(handler.getTime(), equalTo(expectedTime));
    assertThat(handler.getDuration(), equalTo(expectedDuration));
    assertThat(handler.getWebsite(), equalTo(expectedWebsite));
    assertThat(handler.getVideoUrl(Resolution.SMALL), equalTo(expectedUrlSmall));
    assertThat(handler.getVideoUrl(Resolution.NORMAL), equalTo(expectedUrlNormal));
    assertThat(handler.getVideoUrl(Resolution.HD), equalTo(expectedUrlHd));
    assertThat(handler.getSubtitle(), equalTo(expectedSubtitle));
  }
}
