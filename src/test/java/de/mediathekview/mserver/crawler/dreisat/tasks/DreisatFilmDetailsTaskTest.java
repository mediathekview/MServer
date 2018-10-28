package de.mediathekview.mserver.crawler.dreisat.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class DreisatFilmDetailsTaskTest extends DreisatTaskTestBase {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/mediathek/?mode=play&obj=73947",
            "/mediathek/xmlservice.php/v3/web/beitragsDetails?id=73947",
            "/dreisat/dreisat_film_details.xml",
            "/tmd/2/ngplayer_2_3/vod/ptmd/3sat/180531_schoenbrunn_sendung_musik/4",
            "/dreisat/dreisat_film_details.json",
            "Musik",
            "Sommernachtskonzert Schönbrunn 2018",
            LocalDateTime.of(2018, 10, 27, 8, 30, 0),
            Duration.ofHours(1).plusMinutes(18).plusSeconds(44),
            "Vor der Traumkulisse von Schloss Schönbrunn werden bei freiem Eintritt Arien italienischer Klassiker wie Giacomo Puccini, Francesco Cilea und Ruggero Leoncavallo erklingen. Außerdem stehen...",
            "http://localhost:8589/mediathek/?mode=play&obj=73947",
            "http://localhost:8589/rodlzdf-a.akamaihd.net/dach/3sat/18/05/180531_schoenbrunn_sendung_musik/4/180531_schoenbrunn_sendung_musik_476k_p9v13.mp4",
            "http://localhost:8589/rodlzdf-a.akamaihd.net/dach/3sat/18/05/180531_schoenbrunn_sendung_musik/4/180531_schoenbrunn_sendung_musik_2328k_p35v13.mp4",
            "http://localhost:8589/rodlzdf-a.akamaihd.net/dach/3sat/18/05/180531_schoenbrunn_sendung_musik/4/180531_schoenbrunn_sendung_musik_3328k_p36v13.mp4",
            "",
            GeoLocations.GEO_DE_AT_CH,
            true
        }
    });
  }

  private String requestUrl;
  private final String filmUrl;
  private final String filmXmlFile;
  private final String videoUrl;
  private final String videoJsonFile;
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
  private final boolean optimizeUrls;

  public DreisatFilmDetailsTaskTest(final String aRequestUrl, final String aFilmUrl, final String aFilmXmlFile, final String aVideoUrl,
      final String aVideoJsonFile, final String aExpectedTopic, final String aExpectedTitle,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration, final String aExpectedDescription,
      final String aExpectedWebsite, final String aExpectedUrlSmall, final String aExpectedUrlNormal,
      final String aExpectedUrlHd, final String aExpectedSubtitle, final GeoLocations aExpectedGeo, final boolean aOptimizeUrls) {
    requestUrl = aRequestUrl;
    filmUrl = aFilmUrl;
    filmXmlFile = aFilmXmlFile;
    videoUrl = aVideoUrl;
    videoJsonFile = aVideoJsonFile;
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
    optimizeUrls = aOptimizeUrls;
  }

  @Test
  public void test() {
    setupSuccessfulXmlResponse(filmUrl, filmXmlFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);

    if (optimizeUrls) {
      setupHeadResponse(200);
    } else {
      setupHeadResponse(404);
    }

    final Set<Film> actual = executeTask();

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm
        .assertEquals(film, Sender.DREISAT, expectedTopic, expectedTitle, expectedTime, expectedDuration, expectedDescription,
            expectedWebsite,
            new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }

  private Set<Film> executeTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + requestUrl));
    return new DreisatFilmDetailsTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE, WireMockTestBase.MOCK_URL_BASE).invoke();
  }
}