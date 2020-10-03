package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OrfLetterPageTaskTest {

  private static final String ORF_EMPTY_PAGE = "/orf/orf_letter_empty.html";

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private final TopicUrlDTO[] expectedUrls =
      new TopicUrlDTO[] {
        new TopicUrlDTO(
            "Adj'Isten magyarok",
            "https://tvthek.orf.at/profile/AdjIsten-magyarok/13886441/AdjIsten-magyarok/14007007"),
        new TopicUrlDTO(
            "Aktuell in Österreich",
            "https://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/14007906"),
        new TopicUrlDTO(
            "Alltagsgeschichten",
            "https://tvthek.orf.at/profile/Alltagsgeschichten/13887428/Alltagsgeschichte-Am-Wuerstelstand/14007519"),
        new TopicUrlDTO(
            "Alpine Ski World Cup Magazin",
            "https://tvthek.orf.at/profile/Alpine-Ski-World-Cup-Magazin/13889761/FIS-Alpine-SKI-World-Cup-Magazin-2018-2019-Folge-16/14007422"),
        new TopicUrlDTO(
            "Am Schauplatz Gericht",
            "https://tvthek.orf.at/profile/Am-Schauplatz-Gericht/13886290/Am-Schauplatz-Gericht-Der-glaubt-ich-bin-deppert/14007403"),
        new TopicUrlDTO(
            "Aufgetischt",
            "https://tvthek.orf.at/profile/Aufgetischt/13886333/Aufgetischt-am-Sonntag-Dornbirn/14007714"),
        new TopicUrlDTO(
            "Aus dem Rahmen",
            "https://tvthek.orf.at/profile/Aus-dem-Rahmen/3078207/Aus-dem-Rahmen-Kunst-Karl-und-die-Basken-Das-Guggenheim-Museum-in-Bilbao/14007927"),
        new TopicUrlDTO(
            "Autofocus",
            "https://tvthek.orf.at/profile/Autofocus/13886508/Autofocus-LKW-Gas-und-Strom-statt-Diesel/14007294"),
        new TopicUrlDTO(
            "Yoga-Magazin",
            "https://tvthek.orf.at/profile/Yoga-Magazin/7708946/Das-Yoga-Magazin-Folge-110/14007507")
      };

  @Test
  public void test() throws Exception {
    final OrfCrawler crawler = Mockito.mock(OrfCrawler.class);

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(
        OrfConstants.URL_SHOW_LETTER_PAGE + "A",
        "/orf/orf_letter_multiple_themes_multiple_films.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "B", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "C", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "D", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "E", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "F", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "G", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "H", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "I", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "J", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "K", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "L", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "M", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "N", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "O", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "P", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Q", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "R", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "S", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "T", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "U", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "V", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "W", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "X", ORF_EMPTY_PAGE);
    urlMapping.put(
        OrfConstants.URL_SHOW_LETTER_PAGE + "Y", "/orf/orf_letter_single_theme_single_film.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Z", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "0", ORF_EMPTY_PAGE);

    urlMapping.forEach(
        (url, fileName) -> {
          try {
            final Document document = JsoupMock.getFileDocument(url, fileName);
            when(jsoupConnection.getDocumentTimeoutAfter(eq(url), anyInt())).thenReturn(document);

          } catch (final IOException iox) {
            fail();
          }
        });

    when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.ORF));

    final OrfLetterPageTask target = new OrfLetterPageTask(crawler, jsoupConnection);

    final Queue<TopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedUrls.length));
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
