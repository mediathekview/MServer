package de.mediathekview.mserver.crawler.orf.tasks;

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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
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
    return Arrays.asList(new Object[][] { 
      { 
        "http://tvthek.orf.at/profile/100-Jahre-Simpl/13888311/100-Jahre-Simpl/13962984",
        "/orf/orf_film_with_subtitle.html",
        "100 Jahre Simpl",
        "100 Jahre Simpl",
        LocalDateTime.of(2018, 1, 28, 0, 0, 0),
        Duration.of(2658, ChronoUnit.SECONDS),
        "Das Beste aus dem Simpl der letzten 50 Jahre aus der Ära Michael Niavarani, Albert Schmidtleitner Mit Christoph Fälbl, Roman Frankl, Vikor Gernot, Sigrid Hauser, Michael A. Mohapp, Bernhard Murg, Michael Niavarani, Steffi Paschke, Alexandra Schmid, Bettina Soriat, Herbert Steinböck u.v.a.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/32/b57f2d8b019f1f0ad3471d85226c123db557b831.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/446dc6484f90d21fdc2614274f3f7126/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/0b0004b823a3c06c9d44e2b68868f1cf/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/19ebe3c3bb148b8e8251eca0b7cae0c8/1517173709/20180128_0000_sd_02_100-JAHRE-SIMPL_100-Jahre-Simpl__13962984__o__3727549265__s14227590_0__WEB03HD_00102507P_00544307P_Q8C.mp4",
        new GeoLocations[] { GeoLocations.GEO_NONE }
      },
      {
        "http://tvthek.orf.at/profile/Mountain-Attack/13886812/Mountain-Attack-Highlights-aus-Saalbach/13962229",
        "/orf/orf_film_no_subtitle.html",
        "Mountain Attack, Highlights aus Saalbach",
        "Mountain Attack, Highlights aus Saalbach",
        LocalDateTime.of(2018, 1,22, 19, 30, 00),
        Duration.of(1013, ChronoUnit.SECONDS),
        "Bei der 20. Mountain Attack im Jänner 2018 bezwangen die Tourenskisportler sechs Gipfel und 3.008 Höhenmeter auf einer Strecke von 40 Kilometern.",
        "",
        "http://localhost:8589/apasfpd.apa.at/cms-austria/online/b81830be5e344d34259b9cb8c747977f/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-austria/online/e959ba55a87acd553e04296c196fd079/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-austria/online/12a33dab839ecd322a3440600a147f31/1517173787/20180122_1930_sd_03_MOUNTAIN-ATTACK_Mountain-Attack__13962229__o__1876614391__s14223582_2__ORFSHD_19391812P_19561116P_Q8C.mp4",
        new GeoLocations[] { GeoLocations.GEO_AT }
      },
      {
        "http://tvthek.orf.at/profile/Das-ewige-Leben/13886855/Das-ewige-Leben/13963129",
        "/orf/orf_film_duration_hour.html",
        "Das ewige Leben",
        "Das ewige Leben",
        LocalDateTime.of(2018, 1, 27, 20, 15, 5),
        Duration.of(113, ChronoUnit.MINUTES),
        "Hinweis der Redaktion: Aus Gründen des Jugendschutzes ist diese Sendung nur zwischen 20.00 Uhr und 6.00 Uhr als Video on demand abrufbar. Brenner kehrt nach Graz zurück, in die Stadt seiner Jugend. In der Konfrontation mit seinen Jugendfreunden, seiner Jugendliebe und seiner großen Jugendsünde, kommt es zu Morden und zu einem verhängnisvollen Kopfschuss.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/30/9518d4ae312b0c9e7484cf9f90e466ab826d4d40.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-austria/online/992ac08cde7be11fac6e50b3510f3fe6/1517342190/20180127_2015_sd_01_DAS-EWIGE-LEBEN_Das-ewige-Leben__13963129__o__9790399045__s14227570_0__ORF1HD_20151918P_22090618P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-austria/online/f74cb1dbce56813f59cd66999e9d3338/1517342190/20180127_2015_sd_01_DAS-EWIGE-LEBEN_Das-ewige-Leben__13963129__o__9790399045__s14227570_0__ORF1HD_20151918P_22090618P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-austria/online/4f6df57f3acdfcaa2fe8e914699186ce/1517342190/20180127_2015_sd_01_DAS-EWIGE-LEBEN_Das-ewige-Leben__13963129__o__9790399045__s14227570_0__ORF1HD_20151918P_22090618P_Q8C.mp4",
        new GeoLocations[] { GeoLocations.GEO_AT }
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
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/0bb060c0744c962fcacca6eb9211ad70/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/4f512329a47f2cc5b196edb3170d1884/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/7fa882e42a1a23eec93f1310f302478e/1517342250/20161011_1040_in_02_Bundesland-heut_____13890700__o__1693823857__s13890997_Q8C.mp4",
        new GeoLocations[] { GeoLocations.GEO_NONE }
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
        "http://apasfpd.apa.at/cms-worldwide/online/60abae06d715256ceb1548e235db7194/1517698714/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q4A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/0d345417cbbae316a7bdb252dc52484f/1517698714/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q6A.mp4",
        "http://apasfpd.apa.at/cms-worldwide/online/fc56881b62c5828cf321ef51909d4541/1517698714/2000-07-13_1200_in_00_Zweisprachige-Ortsta_____9056913__o__0001362620__s9056914___Q8C.mp4",
        new GeoLocations[] { GeoLocations.GEO_NONE }
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
