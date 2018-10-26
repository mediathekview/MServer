package de.mediathekview.mserver.crawler.orf.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore("javax.net.ssl.*")
public class OrfFilmDetailTaskTest extends OrfFilmDetailTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
            "https://tvthek.orf.at/profile/Rede-des-Bundespraesidenten/13889684/Rede-des-Bundespraesidenten/13993313",
            "/orf/orf_film_with_subtitle.html",
            "Rede des Bundespräsidenten",
            "Rede des Bundespräsidenten",
            LocalDateTime.of(2018, 10, 26, 19, 47, 0),
            Duration.ofMinutes(7).plusSeconds(10),
            "Bundespräsident Alexander Van der Bellen zeigt sich optimistisch, wünscht sich aber, sich an das \"Österreichische\" zu erinnern – also das Gemeinsame vor das Trennende zu stellen.",
            "https://api-tvthek.orf.at/uploads/media/subtitles/0055/75/02ea0c39f7d1f220fbc45284dd13b1d096abd5c8.ttml",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-26_1947_sd_02_Rede-des-Bundes_____13993313__o__1465128264__s14386692_2__ORF2HD_19461317P_19532320P_Q4A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-26_1947_sd_02_Rede-des-Bundes_____13993313__o__1465128264__s14386692_2__ORF2HD_19461317P_19532320P_Q6A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-26_1947_sd_02_Rede-des-Bundes_____13993313__o__1465128264__s14386692_2__ORF2HD_19461317P_19532320P_Q8C.mp4/playlist.m3u8",
            new GeoLocations[]{GeoLocations.GEO_NONE}
        },
        {
            "http://tvthek.orf.at/profile/Mountain-Attack/13886812/Mountain-Attack-Highlights-aus-Saalbach/13962229",
            "/orf/orf_film_no_subtitle.html",
            "Mountain Attack, Highlights aus Saalbach",
            "Mountain Attack, Highlights aus Saalbach",
            LocalDateTime.of(2018, 1, 22, 19, 30, 00),
            Duration.of(1013, ChronoUnit.SECONDS),
            "Bei der 20. Mountain Attack im Jänner 2018 bezwangen die Tourenskisportler sechs Gipfel und 3.008 Höhenmeter auf einer Strecke von 40 Kilometern.",
            "",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-austria/online/b81830be5e344d34259b9cb8c747977f/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q4A.mp4",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-austria/online/e959ba55a87acd553e04296c196fd079/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q6A.mp4",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-austria/online/12a33dab839ecd322a3440600a147f31/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q8C.mp4",
            new GeoLocations[]{GeoLocations.GEO_AT}
        },
        {
            "https://tvthek.orf.at/profile/Hilfe-ich-hab-meine-Lehrerin-geschrumpft/13889696/Hilfe-ich-hab-meine-Lehrerin-geschrumpft/13993284",
            "/orf/orf_film_duration_hour.html",
            "Hilfe, ich hab meine Lehrerin geschrumpft",
            "Hilfe, ich hab meine Lehrerin geschrumpft",
            LocalDateTime.of(2018, 10, 26, 11, 45, 0),
            Duration.of(91, ChronoUnit.MINUTES),
            "Prominent besetztes, turbulentes Abenteuer nach dem gleichnamigen Kinderbuch von Sabine Ludwig.",
            "https://api-tvthek.orf.at/uploads/media/subtitles/0055/68/79bfdb25e01789b3b22661f541dbca4b9a40307b.ttml",
            "https://apasfiis.sf.apa.at/ipad/cms-austria/2018-10-26_1145_sd_01_Hilfe--ich-hab-_____13993284__o__1944471619__s14386477_7__ORF1HD_11453806P_13165216P_Q4A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-austria/2018-10-26_1145_sd_01_Hilfe--ich-hab-_____13993284__o__1944471619__s14386477_7__ORF1HD_11453806P_13165216P_Q6A.mp4/playlist.m3u8",
            "https://apasfiis.sf.apa.at/ipad/cms-austria/2018-10-26_1145_sd_01_Hilfe--ich-hab-_____13993284__o__1944471619__s14386477_7__ORF1HD_11453806P_13165216P_Q8C.mp4/playlist.m3u8",
            new GeoLocations[]{GeoLocations.GEO_AT}
        },
        {
            "http://tvthek.orf.at/profile/BUNDESLAND-HEUTE/8461416/Bundesland-heute/13890700",
            "/orf/orf_film_date_cest.html",
            "Bundesland heute",
            "Bundesland heute",
            LocalDateTime.of(2016, 10, 1, 12, 55, 9),
            Duration.of(30, ChronoUnit.SECONDS),
            "",
            "",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-worldwide/online/0bb060c0744c962fcacca6eb9211ad70/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q4A.mp4",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-worldwide/online/4f512329a47f2cc5b196edb3170d1884/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q6A.mp4",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-worldwide/online/7fa882e42a1a23eec93f1310f302478e/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q8C.mp4",
            new GeoLocations[]{GeoLocations.GEO_NONE}
        },
        {
            "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Zweisprachige-Ortstafeln/9056913",
            "/orf/orf_archive_film.html",
            "Die Geschichte des Burgenlandes",
            "Zweisprachige Ortstafeln",
            LocalDateTime.of(2000, 7, 13, 12, 0, 0),
            Duration.of(174, ChronoUnit.SECONDS),
            "Bundeskanzler Wolfgang Schüssel (ÖVP) hat die erste offizielle zweisprachige Ortstafel im Burgenland am 13. Juli 2000 feierlich enthüllt. \"Burgenland Heute\" berichtet über das Minderheitenrecht, das jahrzehntelang für Diskussionen sorgte. Seit 1955 müssten die zweisprachigen Ortstafeln schon stehen. Der Artikel 7 des Staatsvertrages sieht diese Regelung im Verfassungsrang zwingend vor. Was aber fo\n.....",
            "",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-worldwide/online/60abae06d715256ceb1548e235db7194/1517698714/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q4A.mp4",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-worldwide/online/0d345417cbbae316a7bdb252dc52484f/1517698714/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q6A.mp4",
            "http://localhost:8589/apasfpd.sf.apa.at/cms-worldwide/online/fc56881b62c5828cf321ef51909d4541/1517698714/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q8C.mp4",
            new GeoLocations[]{GeoLocations.GEO_NONE}
        }
    });
  }


  private final String requestUrl;
  private final String filmPageFile;
  private final String theme;
  private final String expectedTitle;
  private final LocalDateTime expectedDate;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final GeoLocations[] expectedGeoLocations;

  public OrfFilmDetailTaskTest(final String aRequestUrl,
      final String aFilmPageFile,
      final String aTheme,
      final String aExpectedTitle,
      final LocalDateTime aExpectedDate,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedSubtitle,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final GeoLocations[] aExpectedGeoLocations) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    theme = aTheme;
    expectedTitle = aExpectedTitle;
    expectedDate = aExpectedDate;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedSubtitle = aExpectedSubtitle;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedGeoLocations = aExpectedGeoLocations;
  }

  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
    JsoupMock.mock(requestUrl, filmPageFile);

    final Set<Film> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));

    Film actualFilm = (Film) actual.toArray()[0];
    AssertFilm.assertEquals(actualFilm,
        Sender.ORF,
        theme,
        expectedTitle,
        expectedDate,
        expectedDuration,
        expectedDescription,
        requestUrl,
        expectedGeoLocations,
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitle
    );
  }

}
