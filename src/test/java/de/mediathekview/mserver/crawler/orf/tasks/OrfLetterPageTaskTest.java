package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mserver.crawler.orf.OrfTopicUrlDTO;
import de.mediathekview.mserver.crawler.orf.OrfConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class OrfLetterPageTaskTest {

  private final OrfTopicUrlDTO[] expectedUrls= new OrfTopicUrlDTO[] {
    new OrfTopicUrlDTO("ABC Bär", "http://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/13962996"),
    new OrfTopicUrlDTO("ABC Bär", "http://tvthek.orf.at/profile/ABC-Baer/4611813/ABC-Baer/13962935"),
    new OrfTopicUrlDTO("Ungarisches Magazin: Adj'Isten magyarok", "http://tvthek.orf.at/profile/AdjIsten-magyarok/13886441/Ungarisches-Magazin-AdjIsten-magyarok/13961067"),
    new OrfTopicUrlDTO("Aktuell in Österreich", "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962830"),
    new OrfTopicUrlDTO("Aktuell in Österreich", "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962694"),
    new OrfTopicUrlDTO("Aktuell in Österreich", "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962553"),
    new OrfTopicUrlDTO("Aktuell in Österreich", "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962390"),
    new OrfTopicUrlDTO("Aktuell in Österreich", "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962219"),
    new OrfTopicUrlDTO("Am Schauplatz", "http://tvthek.orf.at/profile/Am-Schauplatz/1239/Am-Schauplatz-Vor-dem-Nichts-Wenn-die-Delogierung-droht/13962716"),
    new OrfTopicUrlDTO("Aufgetischt am Sonntag", "http://tvthek.orf.at/profile/Aufgetischt/13886333/Aufgetischt-am-Sonntag-Der-Grossglockner/13963022"),
    new OrfTopicUrlDTO("Aus dem Rahmen", "http://tvthek.orf.at/profile/Aus-dem-Rahmen/3078207/Aus-dem-Rahmen-650-Jahre-Nationalbibliothek/13962409"),
    new OrfTopicUrlDTO("Autofocus", "http://tvthek.orf.at/profile/Autofocus/13886508/Autofocus-Entspannter-Reisen-durch-autonomes-Fahren/13962578"),
    new OrfTopicUrlDTO("100 Jahre Simpl", "http://tvthek.orf.at/profile/100-Jahre-Simpl/13888311/100-Jahre-Simpl/13962984")
  };
  
  @Test
  public void test() throws Exception {
    OrfLetterPageTask target = new OrfLetterPageTask();

    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE, "/orf/orf_letter_multiple_themes_multiple_films.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "A", "/orf/orf_letter_multiple_themes_multiple_films.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "B", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "C", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "D", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "E", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "F", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "G", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "H", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "I", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "J", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "K", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "L", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "M", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "N", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "O", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "P", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Q", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "R", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "S", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "T", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "U", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "V", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "W", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "X", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Y", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "Z", "/orf/orf_letter_empty.html");
    urlMapping.put(OrfConstants.URL_SHOW_LETTER_PAGE + "0", "/orf/orf_letter_single_theme_single_film.html");

    JsoupMock.mock(urlMapping);
    ConcurrentLinkedQueue<OrfTopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
