package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class OrfFilmDetailTaskTestWithEpisodes extends OrfFilmDetailTaskTestBase {

  private static final String REQUEST_URL =
      "https://tvthek.orf.at/profile/ZIB-900/71256/ZIB-900/14007767";
  private static final String EXPECTED_THEME = "ZIB 9:00";
  private static final LocalDateTime EXPECTED_TIME = LocalDateTime.of(2019, 3, 18, 9, 0, 0);
  private static final GeoLocations[] EXPECTED_GEO = new GeoLocations[] {GeoLocations.GEO_NONE};

  private static final int INDEX_TITLE = 0;
  private static final int INDEX_DURATION = 1;
  private static final int INDEX_DESCRIPTION = 2;
  private static final int INDEX_SUBTITLE = 3;
  private static final int INDEX_URL_SMALL = 4;
  private static final int INDEX_URL_NORMAL = 5;
  private static final int INDEX_URL_HD = 6;

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private static Object[][] data() {
    return new Object[][] {
      {
        "ZIB 9:00",
        Duration.ofMinutes(8).plusSeconds(38),
        "Neuseeland: Attentäter will sich selbst verteidigen | Jölli (ORF) über Prozess in Chemnitz | Boeing: USA überprüft Zulassungsverfahren | Fünf Jahre Annexion der Krim | Peter Kraus ist 80 | Thiem auf Platz vier der Weltrangliste | Wetter",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/ab9769266a0be435d2fd5d73cabde0076ef9d55c.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/14007767_0011_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/14007767_0011_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/14007767_0011_Q8C.mp4/playlist.m3u8"
      },
      {
        "Thiem auf Platz vier der Weltrangliste",
        Duration.ofSeconds(35),
        "Der Tennisspieler Dominic Thiem hat Superstar Roger Federer geschlagen und erstmals einen Masters-1000-Titel gewonnen. Auf der Weltrangliste steht Thiem nun auf Platz vier.",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/c10d2fa4eda80c8b90d3d449ada6d68736fa4395.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Thiem-auf-Platz__14007767__o__7185021595__s14465213_3__ORF2HD_09063916P_09071423P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Thiem-auf-Platz__14007767__o__7185021595__s14465213_3__ORF2HD_09063916P_09071423P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Thiem-auf-Platz__14007767__o__7185021595__s14465213_3__ORF2HD_09063916P_09071423P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Signation | Themen",
        Duration.ofSeconds(16),
        "Neuseeland: Attentäter will sich selbst verteidigen | Jölli (ORF) über Prozess in Chemnitz | Boeing: USA überprüft Zulassungsverfahren | Fünf Jahre Annexion der Krim | Peter Kraus ist 80 | Thiem auf Platz vier der Weltrangliste | Wetter",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/6d222f74207d1d1dee0d2d4611815951e745e6c3.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Signation---The__14007767__o__1412221432__s14465207_7__ORF2HD_09000021P_09001714P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Signation---The__14007767__o__1412221432__s14465207_7__ORF2HD_09000021P_09001714P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Signation---The__14007767__o__1412221432__s14465207_7__ORF2HD_09000021P_09001714P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Wetter",
        Duration.ofSeconds(85),
        "Nach dem sonnigen und milden Sonntag ist über Nacht feuchte Luft von Nordeuropa nach Österreich geströmt. Damit wird es ein unbeständiger und kühler Wochenstart",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/f1488a72dde1d0dbfc892d431503b5d8e005b75b.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Wetter__14007767__o__1404024135__s14465214_4__ORF2HD_09071423P_09084015P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Wetter__14007767__o__1404024135__s14465214_4__ORF2HD_09071423P_09084015P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Wetter__14007767__o__1404024135__s14465214_4__ORF2HD_09071423P_09084015P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Peter Kraus ist 80",
        Duration.ofSeconds(74),
        "Peter Kraus feiert am Montag seinen 80. Geburtstag. Der österreichische Sänger und Schauspieler ist seit den 1950er Jahren berühmt und hat den Rock & Roll im deutschsprachigen Raum salonfähig gemacht.",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/d5c8eb0e78c1226a38639615ada85592533cf20c.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Peter-Kraus-ist__14007767__o__1330093995__s14465212_2__ORF2HD_09052421P_09063916P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Peter-Kraus-ist__14007767__o__1330093995__s14465212_2__ORF2HD_09052421P_09063916P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Peter-Kraus-ist__14007767__o__1330093995__s14465212_2__ORF2HD_09052421P_09063916P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Jölli (ORF) über Prozess in Chemnitz",
        Duration.ofSeconds(64),
        "In Chemnitz in Deutschland beginnt der Prozess um ein Messerangriff. Als Täter gelten zwei Asylwerber und das löst nach der Tat Aufruhr aus. Andreas Jölli (ORF) berichtet.",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/e9a6bbc0d3a34adaf677a7845ef923abf46c75e1.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Joelli--ORF--ue__14007767__o__1318216483__s14465209_9__ORF2HD_09013812P_09024309P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Joelli--ORF--ue__14007767__o__1318216483__s14465209_9__ORF2HD_09013812P_09024309P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Joelli--ORF--ue__14007767__o__1318216483__s14465209_9__ORF2HD_09013812P_09024309P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Fünf Jahre Annexion der Krim",
        Duration.ofSeconds(85),
        "Vor fünf Jahren hat Russland die ukrainische Halbinsel Krim per Staatsvertrag annektiert. Die Aktion hat viel Kritik ausgelöst, Moskau ist seitdem mit Sanktionen belegt.",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/559b2abe436554513bf9b6d7adc5a6c86976390b.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Fuenf-Jahre-Ann__14007767__o__1979729045__s14465211_1__ORF2HD_09035904P_09052421P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Fuenf-Jahre-Ann__14007767__o__1979729045__s14465211_1__ORF2HD_09035904P_09052421P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Fuenf-Jahre-Ann__14007767__o__1979729045__s14465211_1__ORF2HD_09035904P_09052421P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Boeing: USA überprüft Zulassungsverfahren",
        Duration.ofSeconds(75),
        "Die Abstürze zweier Boeing-Flugzeuge vom Typ 737 Max 8 haben immer mehr Konsequenzen. Die US-Regierung überprüft das Zulassungsverfahren der amerikanischen Flug-Aufsichtsbehörde.",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/39b45f3c67d9db34fa39590756959b78bd036345.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Boeing--USA-ueb__14007767__o__1038723783__s14465210_0__ORF2HD_09024309P_09035904P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Boeing--USA-ueb__14007767__o__1038723783__s14465210_0__ORF2HD_09024309P_09035904P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Boeing--USA-ueb__14007767__o__1038723783__s14465210_0__ORF2HD_09024309P_09035904P_Q8C.mp4/playlist.m3u8"
      },
      {
        "Attentäter will sich selbst verteidigen",
        Duration.ofSeconds(80),
        "Nach dem Terroranschlag in Neuseeland mit mindestens 50 Todesopfern will sich der mutmaßliche Attentäter selbst vor Gericht verteidigen. Der 28-jährige Australier ist wegen Mordes angeklagt.",
        "https://api-tvthek.orf.at/uploads/media/subtitles/0076/23/86e33a5c356a973fcb78527c822f89b1d4fa5709.ttml",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Neuseeland--Att__14007767__o__1967360405__s14465208_8__ORF2HD_09001810P_09013812P_Q4A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Neuseeland--Att__14007767__o__1967360405__s14465208_8__ORF2HD_09001810P_09013812P_Q6A.mp4/playlist.m3u8",
        wireMockServer.baseUrl()
            + "/apasfiis.sf.apa.at/ipad/cms-worldwide/2019-03-18_0900_tl_02_ZIB-9-00_Neuseeland--Att__14007767__o__1967360405__s14465208_8__ORF2HD_09001810P_09013812P_Q8C.mp4/playlist.m3u8"
      }
    };
  }

  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
    final Connection connection =
        JsoupMock.mock(REQUEST_URL, "/orf/orf_film_with_several_parts.html");
    when(jsoupConnection.getConnection(eq(REQUEST_URL))).thenReturn(connection);

    final Object[][] films = data();

    final Set<Film> actual = executeTask(EXPECTED_THEME, REQUEST_URL, jsoupConnection);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(films.length));

    actual.forEach(
        actualFilm -> {
          final Object[] expectedData = getExpectedValues(films, actualFilm.getTitel());
          assertThat(expectedData, notNullValue());

          AssertFilm.assertEquals(
              actualFilm,
              Sender.ORF,
              EXPECTED_THEME,
              expectedData[INDEX_TITLE].toString(),
              EXPECTED_TIME,
              (Duration) expectedData[INDEX_DURATION],
              expectedData[INDEX_DESCRIPTION].toString(),
              REQUEST_URL,
              EXPECTED_GEO,
              expectedData[INDEX_URL_SMALL].toString(),
              expectedData[INDEX_URL_NORMAL].toString(),
              expectedData[INDEX_URL_HD].toString(),
              expectedData[INDEX_SUBTITLE].toString());
        });
  }

  private Object[] getExpectedValues(final Object[][] aExpectedFilms, final String aActualTitle) {
    for (final Object[] expected : aExpectedFilms) {
      if (expected[INDEX_TITLE].toString().compareToIgnoreCase(aActualTitle) == 0) {
        return expected;
      }
    }

    return new Object[0];
  }
}
