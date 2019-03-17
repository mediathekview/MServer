package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
        value = {
                "javax.net.ssl.*",
                "javax.*",
                "com.sun.*",
                "org.apache.logging.log4j.core.config.xml.*"
        })
public class OrfFilmDetailTaskTestWithEpisodes extends OrfFilmDetailTaskTestBase {

    private static final String REQUEST_URL =
            "http://tvthek.orf.at/profile/ZIB-1/1203/ZIB-1/13993106";
  private static final String EXPECTED_THEME = "ZIB 1";
  private static final LocalDateTime EXPECTED_TIME = LocalDateTime.of(2018, 10, 24, 19, 30, 0);
    private static final GeoLocations[] EXPECTED_GEO = new GeoLocations[]{GeoLocations.GEO_NONE};

  private static final int INDEX_TITLE = 0;
  private static final int INDEX_DURATION = 1;
  private static final int INDEX_DESCRIPTION = 2;
  private static final int INDEX_SUBTITLE = 3;
  private static final int INDEX_URL_SMALL = 4;
  private static final int INDEX_URL_NORMAL = 5;
  private static final int INDEX_URL_HD = 6;

  private static Object[][] data() {
      return new Object[][]{
              {
                      "Paketbombenserie in den USA",
                      Duration.of(41, ChronoUnit.SECONDS),
                      "Zwei Wochen vor den Kongresswahlen in den USA sorgt eine mögliche Paketbombenserie für Aufregung. Laut Secret Service sind Sendungen mit potenziellen Sprengsätzen an ehemaligen US-Präsidenten Barrack Obama und Ex-Außenministerin Hillary Clinton abgefangen worden.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Paketbombenseri__13993106__o__1193476538__s14385485_5__ORF2HD_19374106P_19382302P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Paketbombenseri__13993106__o__1193476538__s14385485_5__ORF2HD_19374106P_19382302P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Paketbombenseri__13993106__o__1193476538__s14385485_5__ORF2HD_19374106P_19382302P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Hinweis | Verabschiedung",
                      Duration.of(14, ChronoUnit.SECONDS),
                      "",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hinweis---Verab__13993106__o__1353496418__s14385493_3__ORF2HD_19473405P_19474810P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hinweis---Verab__13993106__o__1353496418__s14385493_3__ORF2HD_19473405P_19474810P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hinweis---Verab__13993106__o__1353496418__s14385493_3__ORF2HD_19473405P_19474810P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Jemen: 14 Millionen von Hunger bedroht",
                      Duration.of(134, ChronoUnit.SECONDS),
                      "Seit vier Jahren tobt im Jemen ein Bürgerkrieg, in dem es um Einfluss und Macht in einer wichtigen Region geht. Mittlerweile leidet die Hälfte der Bevölkerung an Hunger. Laut UN könnten 14 Millionen Menschen bedroht sein.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Jemen--14-Milli__13993106__o__9154233545__s14385488_8__ORF2HD_19400401P_19421819P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Jemen--14-Milli__13993106__o__9154233545__s14385488_8__ORF2HD_19400401P_19421819P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Jemen--14-Milli__13993106__o__9154233545__s14385488_8__ORF2HD_19400401P_19421819P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Pläne für Haus der Geschichte",
                      Duration.of(78, ChronoUnit.SECONDS),
                      "ÖVP-Kulturminister Gernot Blümel hat die neuen Pläne für das Museum \"Haus der Geschichte\" in Wien bekannt gegeben. Unter dem Arbeitstitel \"Haus der Republik\" soll die Institution ein eigenständiges Museum werden.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Plaene-fuer-Hau__13993106__o__9159820185__s14385492_2__ORF2HD_19461512P_19473405P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Plaene-fuer-Hau__13993106__o__9159820185__s14385492_2__ORF2HD_19461512P_19473405P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Plaene-fuer-Hau__13993106__o__9159820185__s14385492_2__ORF2HD_19461512P_19473405P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Ministerrat segnet Kassenreform ab",
                      Duration.of(126, ChronoUnit.SECONDS),
                      "Die Regierung hat im Ministerrat die Reform der Krankenkassen abgesegnet. Der Gesetzesvorschlag geht ohne große Korrekturen ins Parlament, erste Teile sollen schon ab 1. Jänner 2019 gelten.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Ministerrat-seg__13993106__o__5309298085__s14385480_0__ORF2HD_19301604P_19322213P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Ministerrat-seg__13993106__o__5309298085__s14385480_0__ORF2HD_19301604P_19322213P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Ministerrat-seg__13993106__o__5309298085__s14385480_0__ORF2HD_19301604P_19322213P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Asyl für General sorgt für Wirbel",
                      Duration.of(90, ChronoUnit.SECONDS),
                      "Ein Geheimdienstgeneral der syrischen Armee hat 2015 in Österreich Asyl erhalten, obwohl er in Frankreich zuvor bereits abgelehnt worden war. Nun wird gegen Ex-Mitarbeiter des BVT ermittelt.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Asyl-fuer-Gener__13993106__o__1373353656__s14385484_4__ORF2HD_19361019P_19374106P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Asyl-fuer-Gener__13993106__o__1373353656__s14385484_4__ORF2HD_19361019P_19374106P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Asyl-fuer-Gener__13993106__o__1373353656__s14385484_4__ORF2HD_19361019P_19374106P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Meldungen",
                      Duration.of(104, ChronoUnit.SECONDS),
                      "Indexierung der Familienbeihilfe: EU-Kommission droht nach Gesetzesbeschluss | Kopftuchverbot in Kindergärten fixiert | Niki Lauda konnte Krankenhaus verlassen | Ermittlungen gegen Charly Kahr eingestellt | Holocaust-Überlebender Gelbard gestorben",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Meldungen__13993106__o__2114903795__s14385489_9__ORF2HD_19421819P_19440312P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Meldungen__13993106__o__2114903795__s14385489_9__ORF2HD_19421819P_19440312P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Meldungen__13993106__o__2114903795__s14385489_9__ORF2HD_19421819P_19440312P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Opposition kritisiert Spozialversicherungsreform",
                      Duration.of(88, ChronoUnit.SECONDS),
                      "Die Oppositionskritik an der Reform der Krankenkassen reißt nicht ab. Die SPÖ will die Regierungspläne beim Verfassungsgerichtshof anfechten, sagt SPÖ-Chefin Rendi-Wagner.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Opposition-krit__13993106__o__1135953917__s14385481_1__ORF2HD_19322213P_19335024P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Opposition-krit__13993106__o__1135953917__s14385481_1__ORF2HD_19322213P_19335024P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Opposition-krit__13993106__o__1135953917__s14385481_1__ORF2HD_19322213P_19335024P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Claudia Dannhauser (ORF) zur Kassenreform",
                      Duration.of(119, ChronoUnit.SECONDS),
                      "Claudia Dannhauser (ZIB-Innenpolitikredaktion) erläutert, ob eine Anfechtung der Kassenreform beim Verfassungsgerichtshof aussichtsreich ist und was die Reform für die Versicherten bedeutet.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Claudia-Dannhau__13993106__o__5097823565__s14385482_2__ORF2HD_19335024P_19355024P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Claudia-Dannhau__13993106__o__5097823565__s14385482_2__ORF2HD_19335024P_19355024P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Claudia-Dannhau__13993106__o__5097823565__s14385482_2__ORF2HD_19335024P_19355024P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Signation | Themen",
                      Duration.of(42, ChronoUnit.SECONDS),
                      "",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Signation---The__13993106__o__1886650622__s14385479_9__ORF2HD_19293322P_19301604P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Signation---The__13993106__o__1886650622__s14385479_9__ORF2HD_19293322P_19301604P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Signation---The__13993106__o__1886650622__s14385479_9__ORF2HD_19293322P_19301604P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Festspiele Erl: Loebe folgt Kuhn",
                      Duration.of(35, ChronoUnit.SECONDS),
                      "Die Festspiele Erl in Tirol bekommen einen neuen Intendanten. Der Deutsche Bernd Loebe wird im September 2019 - ein Jahr früher als geplant - Gustav Kuhn nachfolgen, dem sexueller Missbrauch vorgeworfen wurde.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Festspiele-Erl-__13993106__o__8847635145__s14385491_1__ORF2HD_19454008P_19461512P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Festspiele-Erl-__13993106__o__8847635145__s14385491_1__ORF2HD_19454008P_19461512P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Festspiele-Erl-__13993106__o__8847635145__s14385491_1__ORF2HD_19454008P_19461512P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Hannelore Veit (ORF) über die verdächtigen Pakete",
                      Duration.of(100, ChronoUnit.SECONDS),
                      "ORF-Korrespondentin Hannelore Veit berichtet, ob es sich bei den potenziellen Paketbomben in den USA um politisch motivierte Anschläge handeln könnte.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hannelore-Veit-__13993106__o__1055118107__s14385487_7__ORF2HD_19382302P_19400401P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hannelore-Veit-__13993106__o__1055118107__s14385487_7__ORF2HD_19382302P_19400401P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hannelore-Veit-__13993106__o__1055118107__s14385487_7__ORF2HD_19382302P_19400401P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "Hinweis",
                      Duration.of(19, ChronoUnit.SECONDS),
                      "",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hinweis__13993106__o__2744303635__s14385483_3__ORF2HD_19355024P_19361019P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hinweis__13993106__o__2744303635__s14385483_3__ORF2HD_19355024P_19361019P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_Hinweis__13993106__o__2744303635__s14385483_3__ORF2HD_19355024P_19361019P_Q8C.mp4/playlist.m3u8"
              },
              {
                      "ZIB 1",
                      Duration.of(18, ChronoUnit.MINUTES).plusSeconds(14),
                      "",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/13993106_0016_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/13993106_0016_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide_episodes/13993106_0016_Q8C.mp4/playlist.m3u8"
              },
              {
                      "ÖBB präsentieren neuen Fahrplan",
                      Duration.of(96, ChronoUnit.SECONDS),
                      "Die ÖBB haben am Mittwoch den neuen Fahrplan vorgestellt, der ab 9. Dezember gilt. Im Nahverkehr kommen zahlreiche neue Bahnverbindungen hinzu.",
                      "",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_OeBB-praesentie__13993106__o__1497058081__s14385490_0__ORF2HD_19440312P_19454008P_Q4A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_OeBB-praesentie__13993106__o__1497058081__s14385490_0__ORF2HD_19440312P_19454008P_Q6A.mp4/playlist.m3u8",
                      "https://apasfiis.sf.apa.at/ipad/cms-worldwide/2018-10-24_1930_tl_02_ZIB-1_OeBB-praesentie__13993106__o__1497058081__s14385490_0__ORF2HD_19440312P_19454008P_Q8C.mp4/playlist.m3u8"
              }
    };
  }

  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
      JsoupMock.mock(REQUEST_URL, "/orf/orf_film_with_several_parts.html");

    Object[][] films = data();

    final Set<Film> actual = executeTask(EXPECTED_THEME, REQUEST_URL);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(films.length));

      actual.forEach(
              actualFilm -> {
                  Object[] expectedData = getExpectedValues(films, actualFilm.getTitel());
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
    for (Object[] expected : aExpectedFilms) {
      if (expected[INDEX_TITLE].toString().compareToIgnoreCase(aActualTitle) == 0) {
        return expected;
      }
    }

    return new Object[0];
  }
}
