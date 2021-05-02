package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.AssertFilm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArdFilmDetailTaskTest extends ArdTaskTestBase {

  private final String filmUrl;
  private final String filmJsonFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedWebsite;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  private final String id;

  public ArdFilmDetailTaskTest(
      final String aId,
      final String aFilmUrl,
      final String aFilmJsonFile,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedWebsite,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeo) {
    id = aId;
    filmUrl = aFilmUrl;
    filmJsonFile = aFilmJsonFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvYTQ2ODI0YjctNThlMy00ODViLTgzMzktNzI1MTJlMjk2ODBi",
            "/page-gateway/pages/ard/item/Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvYTQ2ODI0YjctNThlMy00ODViLTgzMzktNzI1MTJlMjk2ODBi",
            "/ard/ard_film_page11.json",
            "Tagesschau",
            "tagesschau, 09:00 Uhr",
            LocalDateTime.of(2020, 7, 3, 9, 0, 0),
            Duration.ofMinutes(4).plusSeconds(9),
            "Themen der Sendung: Bundestag und Bundesrat stimmen über Kohleausstieg ab, Werbeverbot für Tabak wird verschärft, Großbritannien lockert Corona-Einreisebeschränkungen, Zahl der Corona-Neuinfektionen in den USA erreicht neuen Höchststand, Urteil im Prozess gegen Menschenrechtler Steudtner in Istanbul erwartet, Hongkonger Bürgerrechtsaktivist bittet Deutschland um Hilfe für die Demokratie-Bewegung, \n.....",
            "https://www.ardmediathek.de/video/Y3JpZDovL2Rhc2Vyc3RlLmRlL3RhZ2Vzc2NoYXUvYTQ2ODI0YjctNThlMy00ODViLTgzMzktNzI1MTJlMjk2ODBi",
            "https://media.tagesschau.de/video/2020/0703/TV-20200703-0912-2800.webml.h264.mp4",
            "https://media.tagesschau.de/video/2020/0703/TV-20200703-0912-2800.webl.h264.mp4",
            "https://media.tagesschau.de/video/2020/0703/TV-20200703-0912-2800.webxl.h264.mp4",
            "https://www.ardmediathek.de/subtitle/410890",
            GeoLocations.GEO_NONE,
          }
        });
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);

    final Set<Film> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm.assertEquals(
        film,
        Sender.ARD,
        expectedTopic,
        expectedTitle,
        expectedTime,
        expectedDuration,
        expectedDescription,
        expectedWebsite,
        new GeoLocations[] {expectedGeo},
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitle);
  }

  private Set<Film> executeTask(final String aDetailUrl) {
    final Queue<ArdFilmInfoDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new ArdFilmInfoDto(id, getWireMockBaseUrlSafe() + aDetailUrl, 0));
    return new ArdFilmDetailTask(createCrawler(), urls).invoke();
  }
}
