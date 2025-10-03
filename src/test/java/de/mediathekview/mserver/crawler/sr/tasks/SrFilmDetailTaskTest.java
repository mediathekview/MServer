package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

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

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
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
  public void test() {
    jsoupConnection =
        JsoupMock.mockWithTextModifications(requestUrl, filmPageFile, this::fixupAllWireMockUrls);
    final SrCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    setupSuccessfulJsonResponse(videoDetailsUrl, videoDetailsFile);

    final Set<Film> actual = executeTask(crawler, theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));

    final Film actualFilm = (Film) actual.toArray()[0];
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

  private Set<Film> executeTask(
      final SrCrawler crawler, final String aTheme, final String aRequestUrl) {
    return new SrFilmDetailTask(crawler, createCrawlerUrlDto(aTheme, aRequestUrl)).invoke();
  }
}
