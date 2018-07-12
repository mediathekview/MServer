package de.mediathekview.mserver.crawler.phoenix.parser;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
public class PhoenixFilmXmlHandlerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/phoenix/phoenix_film_detail1.xml",
                "180624_phx_presseclub",
                LocalDateTime.of(2018, 6, 24, 12, 0, 0),
                Duration.ofMinutes(57).plusSeconds(12)
            }
        });
  }

  private final String xmlFile;
  private final String expectedBaseName;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;

  public PhoenixFilmXmlHandlerTest(final String aXmlFile, final String aExpectedBaseName, final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration) {
    xmlFile = aXmlFile;
    expectedBaseName = aExpectedBaseName;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
  }

  @Test
  public void test() throws ParserConfigurationException, SAXException, IOException, URISyntaxException {
    final URI uri = FileReader.class.getResource(xmlFile).toURI();

    SAXParserFactory factory = SAXParserFactory.newInstance();
    SAXParser saxParser = factory.newSAXParser();
    PhoenixFilmXmlHandler handler = new PhoenixFilmXmlHandler();
    saxParser.parse(uri.toString(), handler);

    assertThat(handler.getBaseName(), equalTo(expectedBaseName));
    assertThat(handler.getTime(), equalTo(expectedTime));
    assertThat(handler.getDuration(), equalTo(expectedDuration));
  }
}
