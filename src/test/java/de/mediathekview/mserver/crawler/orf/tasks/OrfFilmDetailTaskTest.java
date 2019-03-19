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
@PowerMockIgnore(
    value = {
      "javax.net.ssl.*",
      "javax.*",
      "com.sun.*",
      "org.apache.logging.log4j.core.config.xml.*"
    })
public class OrfFilmDetailTaskTest extends OrfFilmDetailTaskTestBase {

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

  public OrfFilmDetailTaskTest(
      final String aRequestUrl,
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

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://tvthek.orf.at/profile/Hilfe-ich-hab-meine-Lehrerin-geschrumpft/13889696/Hilfe-ich-hab-meine-Lehrerin-geschrumpft/13993284",
            "/orf/orf_film_duration_hour.html",
            "AD | Tatort",
            "AD | Tatort: Spieglein, Spieglein",
            LocalDateTime.of(2019, 3, 17, 20, 15, 0),
            Duration.ofMinutes(87),
            "Staatsanwältin Klemm ist fassungslos. Die Frau, die mitten auf der Promenade in Münster erschossen wurde, sieht ihr zum Verwechseln ähnlich. Für Kommissar Thiel gibt es zunächst keinerlei Anhaltspunkte für ein Tatmotiv.",
            "",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-17_2015_sd_00_AD---Tatort--Sp_____14007849__o__2088184633__s14465114_4__ORF2ADHD_20144904P_21415115P_Q4A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-17_2015_sd_00_AD---Tatort--Sp_____14007849__o__2088184633__s14465114_4__ORF2ADHD_20144904P_21415115P_Q6A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-17_2015_sd_00_AD---Tatort--Sp_____14007849__o__2088184633__s14465114_4__ORF2ADHD_20144904P_21415115P_Q8C.mp4/playlist.m3u8",
            new GeoLocations[] {GeoLocations.GEO_AT}
          },
          {
            "https://tvthek.orf.at/profile/BUNDESLAND-HEUTE/8461416/Bundesland-heute/13890700",
            "/orf/orf_film_date_cest.html",
            "Bundesland heute",
            "Bundesland heute",
            LocalDateTime.of(2016, 10, 1, 12, 55, 9),
            Duration.ofSeconds(30),
            "",
            "",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-worldwide/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q4A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-worldwide/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q6A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-worldwide/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q8C.mp4/playlist.m3u8",
            new GeoLocations[] {GeoLocations.GEO_NONE}
          },
          {
            "http://tvthek.orf.at/archive/Die-Geschichte-des-Burgenlands/9236430/Zweisprachige-Ortstafeln/9056913",
            "/orf/orf_archive_film.html",
            "Die Geschichte des Burgenlandes",
            "Erste zweisprachige Ortstafel im Burgenland enthüllt",
            LocalDateTime.of(2000, 7, 13, 12, 0, 0),
            Duration.ofSeconds(174),
            "Der Ort Großwarasdorf war die erste burgenländische Gemeinde mit einer offiziellen zweisprachigen Ortstafel. Der damaliger Bundeskanzler Wolfgang Schüssel (ÖVP) war vor Ort, um sie feierlich zu enthüllen. Otkrita prva dvojezična seoska tabla u Gradišću Općina Veliki Borištof je bila prva gradišćanska općina u koj je postavljena oficijelna dvojezična tabla. Tadašnji savezni kancelar Wolfgang Schüss\n.....",
            "",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-worldwide/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q4A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-worldwide/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q6A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-worldwide/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q8C.mp4/playlist.m3u8",
            new GeoLocations[] {GeoLocations.GEO_NONE}
          },
          {
            "https://tvthek.orf.at/profile/Soko-Donau/2672809/Soko-Donau-Entfesselt/14007925",
            "/orf/orf_film_with_subtitle.html",
            "Soko Donau",
            "Soko Donau: Entfesselt",
            LocalDateTime.of(2019, 3, 19, 20, 15, 0),
            Duration.ofMinutes(43).plusSeconds(16),
            "Gewalttäter Gerd Weinzierl kommt nach drei Jahren Haft in elektronisch überwachten Hausarrest. Richard Kofler, Vater von Weinzierls damaligen Opfer Daniela, nimmt das mit großer Sorge wahr.",
            "https://api-tvthek.orf.at/uploads/media/subtitles/0076/35/d184eb43cd1d3a3c926810728cb99ee82204c43e.ttml",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-19_2015_in_01_Soko-Donau--Ent_____14007925__o__2552019395__s14465271_Q4A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-19_2015_in_01_Soko-Donau--Ent_____14007925__o__2552019395__s14465271_Q6A.mp4/playlist.m3u8",
            "http://localhost:8589/apasfiis.sf.apa.at/ipad/cms-austria/2019-03-19_2015_in_01_Soko-Donau--Ent_____14007925__o__2552019395__s14465271_Q8C.mp4/playlist.m3u8",
            new GeoLocations[] {GeoLocations.GEO_AT}
          }
        });
  }

  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
    JsoupMock.mock(requestUrl, filmPageFile);

    final Set<Film> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));

    Film actualFilm = (Film) actual.toArray()[0];
    AssertFilm.assertEquals(
        actualFilm,
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
        expectedSubtitle);
  }
}
