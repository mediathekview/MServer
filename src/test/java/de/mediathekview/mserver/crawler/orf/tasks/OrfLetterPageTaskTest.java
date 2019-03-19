package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
    value = {
      "javax.net.ssl.*",
      "javax.*",
      "com.sun.*",
      "org.apache.logging.log4j.core.config.xml.*"
    })
public class OrfLetterPageTaskTest {

  private static final String ORF_EMPTY_PAGE = "/orf/orf_letter_empty.html";

  private final TopicUrlDTO[] expectedUrls =
      new TopicUrlDTO[] {
        new TopicUrlDTO(
            "ABC Bär", "http://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/13962996"),
        new TopicUrlDTO(
            "ABC Bär", "http://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/13962935"),
        new TopicUrlDTO(
            "Ungarisches Magazin: Adj'Isten magyarok",
            "http://tvthek.orf.at/profile/AdjIsten-magyarok/13886441/Ungarisches-Magazin-AdjIsten-magyarok/13961067"),
        new TopicUrlDTO(
            "Aktuell in Österreich",
            "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962830"),
        new TopicUrlDTO(
            "Aktuell in Österreich",
            "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962694"),
        new TopicUrlDTO(
            "Aktuell in Österreich",
            "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962553"),
        new TopicUrlDTO(
            "Aktuell in Österreich",
            "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962390"),
        new TopicUrlDTO(
            "Aktuell in Österreich",
            "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962219"),
        new TopicUrlDTO(
            "Am Schauplatz",
            "http://tvthek.orf.at/profile/Am-Schauplatz/1239/Am-Schauplatz-Vor-dem-Nichts-Wenn-die-Delogierung-droht/13962716"),
        new TopicUrlDTO(
            "Aufgetischt am Sonntag",
            "http://tvthek.orf.at/profile/Aufgetischt/13886333/Aufgetischt-am-Sonntag-Der-Grossglockner/13963022"),
        new TopicUrlDTO(
            "Aus dem Rahmen",
            "http://tvthek.orf.at/profile/Aus-dem-Rahmen/3078207/Aus-dem-Rahmen-650-Jahre-Nationalbibliothek/13962409"),
        new TopicUrlDTO(
            "Autofocus",
            "http://tvthek.orf.at/profile/Autofocus/13886508/Autofocus-Entspannter-Reisen-durch-autonomes-Fahren/13962578"),
        new TopicUrlDTO(
            "100 Jahre Simpl",
            "http://tvthek.orf.at/profile/100-Jahre-Simpl/13888311/100-Jahre-Simpl/13962984")
      };

  @Test
  public void test() throws Exception {
    final OrfCrawler crawler = Mockito.mock(OrfCrawler.class);
    Mockito.when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.ORF));
    final OrfLetterPageTask target = new OrfLetterPageTask(crawler);

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(
        OrfConstants.URL_SHOW_LETTER_PAGE, "/orf/orf_letter_multiple_themes_multiple_films.html");
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
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "R", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "S", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "T", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "U", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "V", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "W", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "X", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Y", ORF_EMPTY_PAGE);
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Z", ORF_EMPTY_PAGE);
    urlMapping.put(
        OrfConstants.URL_SHOW_LETTER_PAGE + "0", "/orf/orf_letter_single_theme_single_film.html");

    JsoupMock.mock(urlMapping);
    final ConcurrentLinkedQueue<TopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
