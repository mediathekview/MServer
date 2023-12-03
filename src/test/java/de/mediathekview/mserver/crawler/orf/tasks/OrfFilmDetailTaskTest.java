package de.mediathekview.mserver.crawler.orf.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class OrfFilmDetailTaskTest extends OrfFilmDetailTaskTestBase {

  private final String requestUrl;
  private final String filmPageFile;
  private final String theme;
  private final String expectedTheme;
  private final String expectedTitle;
  private final LocalDateTime expectedDate;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedUrlAudioDescriptionSmall;
  private final String expectedUrlAudioDescriptionNormal;
  private final String expectedUrlAudioDescriptionHd;
  private final GeoLocations[] expectedGeoLocations;
  @Mock JsoupConnection jsoupConnection;

  public OrfFilmDetailTaskTest(
      final String aRequestUrl,
      final String aFilmPageFile,
      final String aTheme,
      final String aExpectedTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedDate,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedSubtitle,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedUrlAudioDescriptionSmall,
      final String aExpectedUrlAudioDescriptionNormal,
      final String aExpectedUrlAudioDescriptionHd,
      final GeoLocations[] aExpectedGeoLocations) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    theme = aTheme;
    expectedTheme = aExpectedTheme;
    expectedTitle = aExpectedTitle;
    expectedDate = aExpectedDate;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedUrlSmall = buildWireMockUrl(aExpectedUrlSmall);
    expectedUrlNormal = buildWireMockUrl(aExpectedUrlNormal);
    expectedUrlHd = buildWireMockUrl(aExpectedUrlHd);
    expectedUrlAudioDescriptionSmall = buildWireMockUrl(aExpectedUrlAudioDescriptionSmall);
    expectedUrlAudioDescriptionNormal = buildWireMockUrl(aExpectedUrlAudioDescriptionNormal);
    expectedUrlAudioDescriptionHd = buildWireMockUrl(aExpectedUrlAudioDescriptionHd);
    expectedSubtitle = aExpectedSubtitle;
    expectedGeoLocations = aExpectedGeoLocations;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://tvthek.orf.at/profile/Hilfe-ich-hab-meine-Lehrerin-geschrumpft/13889696/Hilfe-ich-hab-meine-Lehrerin-geschrumpft/13993284",
            "/orf/orf_film_audiodescription_duration_hour.html",
            "AD | Tatort",
            "Tatort",
            "Tatort: Spieglein, Spieglein",
            LocalDateTime.of(2019, 3, 17, 20, 15, 0),
            Duration.ofMinutes(87),
            "Staatsanwältin Klemm ist fassungslos. Die Frau, die mitten auf der Promenade in Münster erschossen wurde, sieht ihr zum Verwechseln ähnlich. Für Kommissar Thiel gibt es zunächst keinerlei Anhaltspunkte für ein Tatmotiv.",
            "",
            "",
            "",
            "",
            "/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-17_2015_sd_00_AD---Tatort--Sp_____14007849__o__2088184633__s14465114_4__ORF2ADHD_20144904P_21415115P_Q4A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-17_2015_sd_00_AD---Tatort--Sp_____14007849__o__2088184633__s14465114_4__ORF2ADHD_20144904P_21415115P_Q6A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-17_2015_sd_00_AD---Tatort--Sp_____14007849__o__2088184633__s14465114_4__ORF2ADHD_20144904P_21415115P_Q8C.mp4/playlist.m3u8",
            new GeoLocations[] {GeoLocations.GEO_AT}
          },
          {
            "https://tvthek.orf.at/profile/BUNDESLAND-HEUTE/8461416/Bundesland-heute/13890700",
            "/orf/orf_film_date_cest.html",
            "Bundesland heute",
            "Bundesland heute",
            "Bundesland heute",
            LocalDateTime.of(2016, 10, 1, 12, 55, 9),
            Duration.ofSeconds(30),
            "",
            "",
            "/apasfiis.sf.apa.at/ipad/cms-worldwide/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q4A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-worldwide/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q6A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-worldwide/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q8C.mp4/playlist.m3u8",
            "",
            "",
            "",
            new GeoLocations[] {GeoLocations.GEO_NONE}
          },
          {
            "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Zweisprachige-Ortstafeln/9056913",
            "/orf/orf_history_film.html",
            "Die Geschichte des Burgenlandes",
            "Die Geschichte des Burgenlandes",
            "Erste zweisprachige Ortstafel im Burgenland enthüllt",
            LocalDateTime.of(2000, 7, 13, 12, 0, 0),
            Duration.ofSeconds(174),
            "Der Ort Großwarasdorf war die erste burgenländische Gemeinde mit einer offiziellen zweisprachigen Ortstafel. Der damaliger Bundeskanzler Wolfgang Schüssel (ÖVP) war vor Ort, um sie feierlich zu enthüllen. Otkrita prva dvojezična seoska tabla u Gradišću Općina Veliki Borištof je bila prva gradišćanska općina u koj je postavljena oficijelna dvojezična tabla. Tadašnji savezni kancelar Wolfgang Schüss\n.....",
            "",
            "/apasfiis.sf.apa.at/ipad/cms-worldwide/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q4A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-worldwide/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q6A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-worldwide/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q8C.mp4/playlist.m3u8",
            "",
            "",
            "",
            new GeoLocations[] {GeoLocations.GEO_NONE}
          },
          {
            "https://tvthek.orf.at/profile/Soko-Donau/2672809/Soko-Donau-Entfesselt/14007925",
            "/orf/orf_film_with_subtitle.html",
            "Soko Donau",
            "Soko Donau",
            "Soko Donau: Entfesselt",
            LocalDateTime.of(2019, 3, 19, 20, 15, 0),
            Duration.ofMinutes(43).plusSeconds(16),
            "Gewalttäter Gerd Weinzierl kommt nach drei Jahren Haft in elektronisch überwachten Hausarrest. Richard Kofler, Vater von Weinzierls damaligen Opfer Daniela, nimmt das mit großer Sorge wahr.",
            "https://api-tvthek.orf.at/uploads/media/subtitles/0076/35/d184eb43cd1d3a3c926810728cb99ee82204c43e.ttml",
            "/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-19_2015_in_01_Soko-Donau--Ent_____14007925__o__2552019395__s14465271_Q4A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-19_2015_in_01_Soko-Donau--Ent_____14007925__o__2552019395__s14465271_Q6A.mp4/playlist.m3u8",
            "/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-19_2015_in_01_Soko-Donau--Ent_____14007925__o__2552019395__s14465271_Q8C.mp4/playlist.m3u8",
            "",
            "",
            "",
            new GeoLocations[] {GeoLocations.GEO_AT}
          },
          {
            "https://tvthek.orf.at/profile/DENK-mit-KULTUR/8728536/DENK-mit-KULTUR-Gerda-Rogers-und-Schiffkowitz/14034271",
            "/orf/orf_film_new_description_block.html",
            "DENK mit KULTUR",
            "DENK mit KULTUR",
            "Gerda Rogers und Schiffkowitz",
            LocalDateTime.of(2019, 12, 6, 21, 5, 0),
            Duration.ofMinutes(45).plusSeconds(0),
            "Birgit Denk hat diesmal Astrologin Gerda Rogers und STS-Star Schiffkowitz zum gemütlichen Late-Night-Talk ins Casino Baden eingeladen.",
            "",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2019-12-06_2105_sd_06_DENK-mit-KULTUR_____14034271__o__1025186593__s14603593_3__ORF3HD_21062006P_21511908P_Q4A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2019-12-06_2105_sd_06_DENK-mit-KULTUR_____14034271__o__1025186593__s14603593_3__ORF3HD_21062006P_21511908P_Q6A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2019-12-06_2105_sd_06_DENK-mit-KULTUR_____14034271__o__1025186593__s14603593_3__ORF3HD_21062006P_21511908P_Q8C.mp4/playlist.m3u8",
            "",
            "",
            "",
            new GeoLocations[] {GeoLocations.GEO_NONE}
          },
          {
            "https://tvthek.orf.at/profile/Burgenland-heute-kompakt/13891025/Burgenland-heute-kompakt/14203644",
            "/orf/orf_film_with_seconds.html",
            "Burgenland heute kompakt",
            "Burgenland heute kompakt",
            "Burgenland heute kompakt",
            LocalDateTime.of(2023, 12, 1, 16, 57, 0),
            Duration.ofSeconds(49),
            "",
            "",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2023-12-01_1657_tl_23_Burgenland-heut_____14203644__o__1338501503__s15520121_1__BLBHD_16570412P_16575304P_Q4A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2023-12-01_1657_tl_23_Burgenland-heut_____14203644__o__1338501503__s15520121_1__BLBHD_16570412P_16575304P_Q6A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2023-12-01_1657_tl_23_Burgenland-heut_____14203644__o__1338501503__s15520121_1__BLBHD_16570412P_16575304P_Q8C.mp4/playlist.m3u8",
            "",
            "",
            "",
            new GeoLocations[] {GeoLocations.GEO_NONE}
          }
        });
  }

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
    jsoupConnection = JsoupMock.mock(requestUrl, filmPageFile);
    OrfCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final Set<Film> actual = executeTask(crawler, theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));

    final Film actualFilm = (Film) actual.toArray()[0];
    AssertFilm.assertEquals(
        actualFilm,
        Sender.ORF,
        expectedTheme,
        expectedTitle,
        expectedDate,
        expectedDuration,
        expectedDescription,
        requestUrl,
        expectedGeoLocations,
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        "",
        "",
        "",
        expectedUrlAudioDescriptionSmall,
        expectedUrlAudioDescriptionNormal,
        expectedUrlAudioDescriptionHd,
        expectedSubtitle);
  }
}
