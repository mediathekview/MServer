package de.mediathekview.mserver.crawler.zdf.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.zdf.ZdfEntryDto;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ZdfFilmDetailTaskTest extends ZdfTaskTestBase {

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "/content/documents/zdf/filme/das-duo/das-duo-echte-kerle-102.json",
            "/zdf/zdf_film_details1.json",
            "/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/160605_echte_kerle_das_duo_neo",
            "/zdf/zdf_video_details1.json",
            "Das Duo",
            "Echte Kerle",
            LocalDateTime.of(2017, 2, 1, 20, 15, 0),
            Duration.ofHours(1).plusMinutes(27).plusSeconds(35),
            "Der Mord an Studienrat Lampert führt \"Das Duo\" an eine Schule, an der Täter und Opfer sich vermutlich begegnet sind. In deren Umfeld suchen Clara Hertz und Marion Ahrens auch das Motiv.",
            "https://www.zdf.de/filme/das-duo/das-duo-echte-kerle-102.html",
            "https://rodlzdf-a.akamaihd.net/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_436k_p9v12.mp4",
            "https://rodlzdf-a.akamaihd.net/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_1456k_p13v12.mp4",
            "https://rodlzdf-a.akamaihd.net/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_3328k_p36v12.mp4",
            "",
            GeoLocations.GEO_NONE
        }
    });
  }

  private final String filmUrl;
  private final String filmJsonFile;
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

  public ZdfFilmDetailTaskTest(final String aFilmUrl, final String aFilmJsonFile, final String aVideoUrl, final String aVideoJsonFile,
      final String aExpectedTopic, final String aExpectedTitle,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration, final String aExpectedDescription,
      final String aExpectedWebsite, final String aExpectedUrlSmall, final String aExpectedUrlNormal,
      final String aExpectedUrlHd, final String aExpectedSubtitle, final GeoLocations aExpectedGeo) {
    filmUrl = aFilmUrl;
    filmJsonFile = aFilmJsonFile;
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
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);

    final Set<Film> actual = executeTask(filmUrl, videoUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm
        .assertEquals(film, Sender.ZDF, expectedTopic, expectedTitle, expectedTime, expectedDuration, expectedDescription, expectedWebsite,
            new GeoLocations[] { expectedGeo }, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }

  private Set<Film> executeTask(final String aDetailUrl, final String aVideoUrl) {
    final ConcurrentLinkedQueue<ZdfEntryDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new ZdfEntryDto(WireMockTestBase.MOCK_URL_BASE + aDetailUrl, WireMockTestBase.MOCK_URL_BASE + aVideoUrl));
    return new ZdfFilmDetailTask(createCrawler(), urls, Optional.empty()).invoke();
  }
}
