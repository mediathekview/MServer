package de.mediathekview.mserver.crawler.sr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class SrFilmDetailTaskTest extends SrTaskTestBase {

  private final String requestUrl;
  private final String filmPageFile;
  private final String videoDetailsUrl;
  private final String videoDetailsFile;
  private final String theme;
  private final String expectedTitle;
  private final LocalDateTime expectedDate;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;

  @Mock
  JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  public SrFilmDetailTaskTest(
      final String aRequestUrl,
      final String aFilmPageFile,
      final String aVideoDetailsUrl,
      final String aVideoDetailsFile,
      final String aTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedDate,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedSubtitle,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    videoDetailsUrl = aVideoDetailsUrl;
    videoDetailsFile = aVideoDetailsFile;
    theme = aTheme;
    expectedTitle = aExpectedTitle;
    expectedDate = aExpectedDate;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedSubtitle = aExpectedSubtitle;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "http://www.sr-mediathek.de/index.php?seite=7&id=77226",
            "/sr/sr_film_page1.html",
            "/sr_player/mc.php?id=77226&tbl=&pnr=0&hd=0&devicetype=",
            "/sr/sr_film_video_details1.json",
            "sportarena extra",
            "sportarena extra - Wo laufen sie denn?",
            LocalDateTime.of(2019, 8, 15, 0, 0, 0),
            Duration.ofMinutes(29).plusSeconds(55),
            "Der 15. August ist ein Feiertag im Saarland - auch für die Fans des Pferderennsports. An diesem Tag veranstaltet der Rennclub Saarbrücken traditionell seinen Jahreshöhepunkt. Auch der SR ist traditionell dabei, wenn die Reitprofis aus ganz Deutschland in Güdingen gegeneinander antreten.",
            "",
            "https://srstorage01-a.akamaihd.net/Video/FS/SA/sportarena_20190815_184401_M.mp4",
            "https://srstorage01-a.akamaihd.net/Video/FS/SA/sportarena_20190815_184401_L.mp4",
            "https://srstorage01-a.akamaihd.net/Video/FS/SA/sportarena_20190815_184401_P.mp4"
          },
          {
            "https://www.sr-mediathek.de/index.php?seite=7&id=77119",
            "/sr/sr_film_page2_with_subtitle.html",
            "/sr_player/mc.php?id=77119&tbl=&pnr=0&hd=0&devicetype=",
            "/sr/sr_film_video_details2.json",
            "SAARTHEMA",
            "SAARTHEMA - Schengen",
            LocalDateTime.of(2019, 8, 15, 0, 0, 0),
            Duration.ofMinutes(43).plusSeconds(18),
            "An jeder Grenze der Welt ist es ein Begriff: Schengen heißt eines der wichtigsten Visa, die es heute gibt. Am 14. Juni 1985 unterzeichneten die Vertreter der EG-Staaten Deutschland, Frankreich, Belgien, Niederlande und Luxemburg das Schengener-Abkommen, das im Laufe der Jahre von fast allen EU-Staaten ratifiziert wurde und uns in Europa offene Grenzen gebracht hat.",
            "https://www.sr-mediathek.de/sr_player/ut.php?file=STH_20190815.xml",
            "https://srstorage01-a.akamaihd.net/Video/FS/STH/Schengen_-_Wie_entstand_das_Europa_ohne_Grenzen_SENDEFASSUNG_M.mp4",
            "https://srstorage01-a.akamaihd.net/Video/FS/STH/Schengen_-_Wie_entstand_das_Europa_ohne_Grenzen_SENDEFASSUNG_L.mp4",
            "https://srstorage01-a.akamaihd.net/Video/FS/STH/Schengen_-_Wie_entstand_das_Europa_ohne_Grenzen_SENDEFASSUNG_P.mp4"
          },
        });
  }

  @Test
  public void test() throws IOException {
    Connection connection = JsoupMock.mock(requestUrl, filmPageFile);
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    setupSuccessfulJsonResponse(videoDetailsUrl, videoDetailsFile);

    final Set<Film> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));

    Film actualFilm = (Film) actual.toArray()[0];
    AssertFilm.assertEquals(
        actualFilm,
        Sender.SR,
        theme,
        expectedTitle,
        expectedDate,
        expectedDuration,
        expectedDescription,
        requestUrl,
        new GeoLocations[0],
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitle);
  }

  private Set<Film> executeTask(String aTheme, String aRequestUrl) {
    return new SrFilmDetailTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl), jsoupConnection).invoke();
  }
}
