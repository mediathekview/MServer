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
  private final String videoUrlDgs;
  private final String videoJsonFileDgs;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedWebsite;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedUrlAudioDescriptionSmall;
  private final String expectedUrlAudioDescriptionNormal;
  private final String expectedUrlAudioDescriptionHd;
  private final String expectedUrlSignLanguageSmall;
  private final String expectedUrlSignLanguageNormal;
  private final String expectedUrlSignLanguageHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  private final boolean optimizeUrls;

  public ZdfFilmDetailTaskTest(
      final String aFilmUrl,
      final String aFilmJsonFile,
      final String aVideoUrl,
      final String aVideoJsonFile,
      final String aVideoUrlDgs,
      final String aVideoJsonFileDgs,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedWebsite,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedUrlAudioDescriptionSmall,
      final String aExpectedUrlAudioDescriptionNormal,
      final String aExpectedUrlAudioDescriptionHd,
      final String aExpectedUrlSignLanguageSmall,
      final String aExpectedUrlSignLanguageNormal,
      final String aExpectedUrlSignLanguageHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeo,
      final boolean aOptimizeUrls) {
    filmUrl = aFilmUrl;
    filmJsonFile = aFilmJsonFile;
    videoUrl = aVideoUrl;
    videoJsonFile = aVideoJsonFile;
    videoUrlDgs = aVideoUrlDgs;
    videoJsonFileDgs = aVideoJsonFileDgs;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = buildWireMockUrl(aExpectedUrlSmall);
    expectedUrlNormal = buildWireMockUrl(aExpectedUrlNormal);
    expectedUrlHd = buildWireMockUrl(aExpectedUrlHd);
    expectedUrlAudioDescriptionSmall = buildWireMockUrl(aExpectedUrlAudioDescriptionSmall);
    expectedUrlAudioDescriptionNormal = buildWireMockUrl(aExpectedUrlAudioDescriptionNormal);
    expectedUrlAudioDescriptionHd = buildWireMockUrl(aExpectedUrlAudioDescriptionHd);
    expectedUrlSignLanguageSmall = buildWireMockUrl(aExpectedUrlSignLanguageSmall);
    expectedUrlSignLanguageNormal = buildWireMockUrl(aExpectedUrlSignLanguageNormal);
    expectedUrlSignLanguageHd = buildWireMockUrl(aExpectedUrlSignLanguageHd);
    expectedSubtitle = buildWireMockUrl(aExpectedSubtitle);
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
            "",
            "",
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
            "",
            "",
            "",
            "",
            "",
            "",
            GeoLocations.GEO_NONE,
            false
          },
          {
            "/content/documents/zdf/kinder/jonalu/tanz-auf-dem-seil-102.json",
            "/zdf/zdf_film_details3.json",
            "/tmd/2/ngplayer_2_3/vod/ptmd/tivi/160301_folge25_tanzaufdemseil_jon",
            "/zdf/zdf_video_details3.json",
            "",
            "",
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
            "",
            "",
            "",
            "",
            "",
            "",
            GeoLocations.GEO_DE_AT_CH,
            true
          },
          {
            "/content/documents/zdf/filme/montagskino/schatz-nimm-du-sie-100.json",
            "/zdf/zdf_film_details_with_audiodescription.json",
            "/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/190715_schatz_nimm_du_sie_mok",
            "/zdf/zdf_video_details_with_audiodescription.json",
            "",
            "",
            "Montagskino",
            "Schatz, nimm du sie!",
            LocalDateTime.of(2019, 7, 15, 22, 15, 0),
            Duration.ofMinutes(89).plusSeconds(1),
            "Ein Elternpaar will sich trennen: Routine im Zeitalter der Patchworkfamilie. Doch keiner will die Kinder, weil sie kurzfristigen Karriereplänen im Wege stehen. (Film: FSK 12)",
            "https://www.zdf.de/filme/montagskino/schatz-nimm-du-sie-100.html",
            "/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a1a2_776k_p11v14.mp4",
            "/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a1a2_1496k_p13v14.mp4",
            "",
            "/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_776k_p11v14.mp4",
            "/de/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/190715_schatz_nimm_du_sie_mok_a3a4_1496k_p13v14.mp4",
            "",
            "",
            "",
            "",
            "https://utstreaming.zdf.de/mtt/zdf/19/07/190715_schatz_nimm_du_sie_mok/4/F1021200_hoh_deu_Schatz_nimm_du_sie_150719.xml",
            GeoLocations.GEO_DE,
            false
          },
          {
            "/content/documents/zdf/dokumentation/zdfinfo-doku/leben-auf-der-strasse-obdachlos-und-abgehaengt-100.json",
            "/zdf/zdf_film_details_with_dgs.json",
            "/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/220505_geliebt_geduldet_getoetet_inf/4",
            "/zdf/zdf_video_details_with_dgs_default.json",
            "/tmd/2/ngplayer_2_3/vod/ptmd/mediathek/220505_geliebt_geduldet_getoetet_inf_dgs/2",
            "/zdf/zdf_video_details_with_dgs.json",
            "ZDFinfo Doku",
            "Geliebt, geduldet, getötet",
                  LocalDateTime.of(2022, 5, 5, 18, 0, 0),
                  Duration.ofMinutes(44).plusSeconds(24),
                  "Trotz aller Bemühung um Inklusion erleben Menschen mit Behinderungen in Deutschland immer noch täglich Ablehnung und Ausgrenzung. Warum ist das so? Und war das schon immer so?",
                  "https://www.zdf.de/dokumentation/zdfinfo-doku/geliebt-geduldet-getoetet-die-geschichte-von-menschen-mit-behinderungen-100.html",
            "/none/zdf/22/05/220505_geliebt_geduldet_getoetet_inf/4/220505_geliebt_geduldet_getoetet_inf_a1a2_808k_p11v15.mp4",
            "/none/zdf/22/05/220505_geliebt_geduldet_getoetet_inf/4/220505_geliebt_geduldet_getoetet_inf_a1a2_1628k_p13v15.mp4",
            "",
            "/none/zdf/22/05/220505_geliebt_geduldet_getoetet_inf/4/220505_geliebt_geduldet_getoetet_inf_a3a4_808k_p11v15.mp4",
            "/none/zdf/22/05/220505_geliebt_geduldet_getoetet_inf/4/220505_geliebt_geduldet_getoetet_inf_a3a4_1628k_p13v15.mp4",
            "",
            "/none/zdf/22/05/220505_geliebt_geduldet_getoetet_inf_dgs/1/220505_geliebt_geduldet_getoetet_inf_dgs_808k_p11v15.mp4",
            "/none/zdf/22/05/220505_geliebt_geduldet_getoetet_inf_dgs/1/220505_geliebt_geduldet_getoetet_inf_dgs_1628k_p13v15.mp4",
            "",
            "https://utstreaming.zdf.de/mtt/zdf/22/05/220505_geliebt_geduldet_getoetet_inf/4/F1034810_hoh_deu_Geliebt_geduldet_getoetet_Menschen_mit_Behinderung_280422.xml",
            GeoLocations.GEO_NONE,
            false
          }
        });
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);
    if (!videoUrlDgs.isEmpty()) {
      setupSuccessfulJsonResponse(videoUrlDgs, videoJsonFileDgs);
    }

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
        expectedUrlSignLanguageSmall,
        expectedUrlSignLanguageNormal,
        expectedUrlSignLanguageHd,
        expectedUrlAudioDescriptionSmall,
        expectedUrlAudioDescriptionNormal,
        expectedUrlAudioDescriptionHd,
        expectedSubtitle);
  }

  private Set<Film> executeTask(final String detailUrl) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + detailUrl));
    return new ZdfFilmDetailTask(createCrawler(), getWireMockBaseUrlSafe(), urls, null).invoke();
  }
}
