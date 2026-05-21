package de.mediathekview.mserver.crawler.sr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class SrFilmDetailTaskTest extends SrTaskTestBase {

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
          {
            "https://www.sr-mediathek.de/index.php?seite=7&id=77119",
            "/sr/sr_film_page3_missing_host_in_url.html",
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
          }
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void test(final String requestUrl, final String filmPageFile, final String videoDetailsUrl, final String videoDetailsFile, final String theme, final String expectedTitle, final LocalDateTime expectedDate, final Duration expectedDuration, final String expectedDescription, final String expectedSubtitle, final String expectedUrlSmall, final String expectedUrlNormal, final String expectedUrlHd) {
    JsoupConnection jsoupConnection =
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
    return new SrFilmDetailTask(crawler, createCrawlerUrlDto(aTheme, aRequestUrl), getWireMockBaseUrlSafe()).invoke();
  }
}
