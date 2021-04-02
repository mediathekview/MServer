package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
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
public class ZdfFilmDetailTaskTest extends ZdfTaskTestBase {

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
  private final boolean optimizeUrls;

  public ZdfFilmDetailTaskTest(
      final String aFilmUrl,
      final String aFilmJsonFile,
      final String aVideoUrl,
      final String aVideoJsonFile,
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
      final GeoLocations aExpectedGeo,
      final boolean aOptimizeUrls) {
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
    expectedUrlSmall =
        aExpectedUrlSmall.isEmpty() ? "" : getWireMockBaseUrlSafe() + aExpectedUrlSmall;
    expectedUrlNormal =
        aExpectedUrlNormal.isEmpty() ? "" : getWireMockBaseUrlSafe() + aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd.isEmpty() ? "" : getWireMockBaseUrlSafe() + aExpectedUrlHd;
    expectedSubtitle =
        aExpectedSubtitle.isEmpty() ? "" : getWireMockBaseUrlSafe() + aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
    optimizeUrls = aOptimizeUrls;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
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
            "/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_436k_p9v12.mp4",
            "/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_1456k_p13v12.mp4",
            "/none/zdf/16/06/160605_echte_kerle_das_duo_neo/6/160605_echte_kerle_das_duo_neo_3328k_p36v12.mp4",
            "",
            GeoLocations.GEO_NONE,
            false
          },
          {
            "/content/documents/zdf/kinder/jonalu/tanz-auf-dem-seil-102.json",
            "/zdf/zdf_film_details3.json",
            "/tmd/2/ngplayer_2_3/vod/ptmd/tivi/160301_folge25_tanzaufdemseil_jon",
            "/zdf/zdf_video_details3.json",
            "JoNaLu",
            "Tanz auf dem Seil - Folge 25",
            LocalDateTime.of(2018, 3, 11, 9, 50, 0),
            Duration.ofMinutes(24).plusSeconds(55),
            "Naya verliert beim Seiltanz ihre Glücksblume und alles geht schief. Kann ein anderer Glücksbringer helfen? Glühwürmchen Minou hat eine \"leuchtende\" Idee.",
            "https://www.zdf.de/kinder/jonalu/tanz-auf-dem-seil-102.html",
            "/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_436k_p9v12.mp4",
            "/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_2328k_p35v12.mp4",
            "/dach/tivi/16/03/160301_folge25_tanzaufdemseil_jon/5/160301_folge25_tanzaufdemseil_jon_3328k_p36v12.mp4",
            "",
            GeoLocations.GEO_DE_AT_CH,
            true
          }
        });
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);

    if (optimizeUrls) {
      setupHeadResponse(200);
    } else {
      setupHeadResponse(404);
    }

    final Set<Film> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm.assertEquals(
        film,
        Sender.ZDF,
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

  private Set<Film> executeTask(final String detailUrl) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + detailUrl));
    return new ZdfFilmDetailTask(createCrawler(), getWireMockBaseUrlSafe(), urls, null).invoke();
  }
}
