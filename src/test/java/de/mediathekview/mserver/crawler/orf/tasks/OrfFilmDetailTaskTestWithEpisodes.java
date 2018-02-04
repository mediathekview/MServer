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
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore("javax.net.ssl.*")
public class OrfFilmDetailTaskTestWithEpisodes extends OrfFilmDetailTaskTestBase {
  
  private static final String REQUEST_URL = "http://tvthek.orf.at/profile/Aktuell-in-Oesterreich/13887571/Aktuell-in-Oesterreich/13962830";
  private static final String EXPECTED_THEME = "Aktuell in Österreich";
  private static final LocalDateTime EXPECTED_TIME = LocalDateTime.of(2018, 1, 26, 17, 5, 0);
  private static final GeoLocations[] EXPECTED_GEO = new GeoLocations[] { GeoLocations.GEO_NONE };

  private static final int INDEX_TITLE = 0;
  private static final int INDEX_DURATION = 1;
  private static final int INDEX_DESCRIPTION = 2;
  private static final int INDEX_SUBTITLE = 3;
  private static final int INDEX_URL_SMALL = 4;
  private static final int INDEX_URL_NORMAL = 5;
  private static final int INDEX_URL_HD = 6;
  
  private static Object[][] data() {
    return new Object[][] { 
      {
        EXPECTED_THEME,
        Duration.of(1329, ChronoUnit.SECONDS),
        "",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/995219cf13e982e87924383384833f3405b74015.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/30744e157f6789f34617bc3c2114770a/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9d3a9fa724a3e0615b018303e8bb558c/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/2db28f0f9086cf63b39c19165b1fc65d/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q8C.mp4"
      },
      {
        "Signation | Headlines",
        Duration.of(47, ChronoUnit.SECONDS),
        "",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/995219cf13e982e87924383384833f3405b74015.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/30744e157f6789f34617bc3c2114770a/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9d3a9fa724a3e0615b018303e8bb558c/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/2db28f0f9086cf63b39c19165b1fc65d/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Signation---Hea__13962830__o__9480239995__s14226895_5__WEB03HD_17071004P_17075707P_Q8C.mp4"
      },
      {
        "Stiwoll: Belohnung für Hinweise",
        Duration.of(139, ChronoUnit.SECONDS),
        "Vor knapp drei Monaten griff ein 66-jähriger Steirer in Stiwoll zur Waffe und erschoss zwei Nachbarn. Nun setzt die Polizei eine Belohnung von 5.000 Euro für neue Hinweise aus, die zur Ergreifung dieses Mannes führen.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/35fd3882d3d8b03403d60363031a8c681ad155e5.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9f415ec2268ec5a00196711cf52aca74/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/49990a5fb68d9c57d98f92018977fa46/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/3fccb7e7f9dfb462265d262b3267da91/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Stiwoll--Belohn__13962830__o__4177600915__s14226896_6__WEB03HD_17075707P_17101611P_Q8C.mp4"
      },
      {
        "Großbrand in Südtirol",
        Duration.of(78, ChronoUnit.SECONDS),
        "In Missian im Überetsch in Südtirol ist ein Mehrfamilienhaus abgebrannt. 16 Personen konnten sich gerade noch ins Freie retten - vier wurden mit Verdacht auf Rauchgasvergiftung in ein Krankenhaus gebracht.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/f11c4c21ef3ccb48c463bbd7513890458db8b518.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/c7f927ba1c4bc34a9d7aab6cc1a71af0/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Grossbrand-in-S__13962830__o__1964990432__s14226897_7__WEB03HD_17101611P_17113413P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/8425ed417876397dcaf530ceca21b49d/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Grossbrand-in-S__13962830__o__1964990432__s14226897_7__WEB03HD_17101611P_17113413P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/be9e47ceea9da43d5444746d24351e20/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Grossbrand-in-S__13962830__o__1964990432__s14226897_7__WEB03HD_17101611P_17113413P_Q8C.mp4"
      },
      {
        "Eiskletterer tot aufgefunden",
        Duration.of(61, ChronoUnit.SECONDS),
        "In Gröden in Südtirol wurde ein zwei Tage lang vermisster Eiskletterer tot geborgen. Der 21-Jährige aus Bayern war in einem Eisfall von einer Lawine erfasst und verschüttet worden.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/57f289f20445912c5ab51d3ed8b2b9455a7bee45.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/019e68bb9b015abbbecb3b49fcc3f336/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Eiskletterer-to__13962830__o__7821221035__s14226898_8__WEB03HD_17113413P_17123603P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/6690b390a67edafe21af403bee4f6654/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Eiskletterer-to__13962830__o__7821221035__s14226898_8__WEB03HD_17113413P_17123603P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/21be161276116a146545c78bc1d92853/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Eiskletterer-to__13962830__o__7821221035__s14226898_8__WEB03HD_17113413P_17123603P_Q8C.mp4"
      },
      {
        "Lkw eineinhalb Tage nonstop unterwegs",
        Duration.of(81, ChronoUnit.SECONDS),
        "Verkehrspolizisten in Oberösterreich haben einen serbischen Lkw-Lenker gestoppt, der eineinhalb Tage lang ohne Pause unterwegs war. Die Beamten haben ihm daher eine Zwangspause verordnet.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/429c213ef5e8e3c0200ff6c07cd3b8731aabffbf.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/fd79c7302c9c1eb3e3d0192f33c9f8e5/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Lkw-eineinhalb-__13962830__o__1102011523__s14226899_9__WEB03HD_17123603P_17135710P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9c1be2187484b2db597d1727db1c72bd/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Lkw-eineinhalb-__13962830__o__1102011523__s14226899_9__WEB03HD_17123603P_17135710P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/9db3fc77f101a9e1bf98cfe58d9e0b17/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Lkw-eineinhalb-__13962830__o__1102011523__s14226899_9__WEB03HD_17123603P_17135710P_Q8C.mp4"
      },
      {
        "Überwachung per Hubschrauber in Davos",
        Duration.of(148, ChronoUnit.SECONDS),
        "Damit alle prominenten Gäste in Davos sicher sind, auch bei der An- und Abreise, wird der Luftraum genau überwacht. In Vorarlberg übernimmt das das Bundesheer, mit der sogenannten Operation Dädalus.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/f84ab754e30e14d37db60ee0dc99804ee918909c.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/55cc0c3f35a8c26b37bdba72a8a89887/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Ueberwachung-pe__13962830__o__8111610145__s14226901_1__WEB03HD_17135710P_17162602P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/f9ad5bd9666996e58effad017844b64f/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Ueberwachung-pe__13962830__o__8111610145__s14226901_1__WEB03HD_17135710P_17162602P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/a01f9827d20f42dc0fae6fa30638d1ea/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Ueberwachung-pe__13962830__o__8111610145__s14226901_1__WEB03HD_17135710P_17162602P_Q8C.mp4"
      },
      {
        "Grippewelle mit Komplikationen",
        Duration.of(124, ChronoUnit.SECONDS),
        "Die Grippewelle steuert hierzulande auf ihren Höhepunkt zu. Allein in der letzten Woche hat sich die Zahl der Grippekranken in Oberösterreich verdoppelt. Mehrere Spitäler haben Isolierstationen eingerichtet.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/00bb634af2f6bb5ff8648603671155aa107fb58e.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/d71a010b52fe2727530973c0bfa0ffbb/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Grippewelle-mit__13962830__o__2072870046__s14226902_2__WEB03HD_17162602P_17183019P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/07fc8e5d0fb4efe6cd70aa835cad4f2c/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Grippewelle-mit__13962830__o__2072870046__s14226902_2__WEB03HD_17162602P_17183019P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/10e65a11cf502d69af4e5364dcaa8d25/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Grippewelle-mit__13962830__o__2072870046__s14226902_2__WEB03HD_17162602P_17183019P_Q8C.mp4"
      },
      {
        "Peter Teubenbacher (ORF) vor der Wiener Universität",
        Duration.of(87, ChronoUnit.SECONDS),
        "ORF-Reporter Peter Teubenbacher meldet sich vor der Wiener Universität, einem Treffpunkt der Demonstranten gegen den Akademikerball.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/2ef142d445b9941804048844d3e359a4dc4a2f3b.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/b3e1cd01df20e0357b68769d9e070b38/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Peter-Teubenbac__13962830__o__1781931379__s14226973_3__WEB03HD_17183019P_17195819P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/91e61a340edcd69d8da415eb0a9a609b/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Peter-Teubenbac__13962830__o__1781931379__s14226973_3__WEB03HD_17183019P_17195819P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/12d623ca9328db998b38f50a52212e05/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Peter-Teubenbac__13962830__o__1781931379__s14226973_3__WEB03HD_17183019P_17195819P_Q8C.mp4"
      },
      {
        "Matthias Schrom (ORF) vor der Wiener Hofburg",
        Duration.of(94, ChronoUnit.SECONDS),
        "ORF-Reporter Matthias Schrom meldet sich vor der Wiener Hofburg, innerhalb der Sperrzone für den Akademikerball.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/d291345bb7665ea650aaa2129c1a25d14ee8bc8c.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/827216499234452d3da42cdad914a807/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Matthias-Schrom__13962830__o__1584444255__s14226900_0__WEB03HD_17195819P_17213313P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/5d4a02df97ebd57ee3a815fe79f2ba3b/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Matthias-Schrom__13962830__o__1584444255__s14226900_0__WEB03HD_17195819P_17213313P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/c82cb9b7dd59a5d2d58d7f4842da4d44/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Matthias-Schrom__13962830__o__1584444255__s14226900_0__WEB03HD_17195819P_17213313P_Q8C.mp4"
      },
      {
        "Gestresste Wildtiere im Winter",
        Duration.of(176, ChronoUnit.SECONDS),
        "Der schneereiche Winter setzt den Wildtieren enorm zu. Wenn sie an den Futterstellen gestört werden, kann das den Tod der Tiere bedeuten. Die Jägerschaft appelliert daher an Freizeitsportler, mehr denn je die Ruhezonen zu beachten.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/4bc5daa5154462204dbfb33a1fdd7bd03f6efdb6.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/6120126b2656c4181c0214cad968f647/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Gestresste-Wild__13962830__o__3760690175__s14226903_3__WEB03HD_17213313P_17243000P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/c5048ff434c85b42edfe6c8c9170bb75/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Gestresste-Wild__13962830__o__3760690175__s14226903_3__WEB03HD_17213313P_17243000P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/178f92b66fcc1060c27f420bf6a74d73/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Gestresste-Wild__13962830__o__3760690175__s14226903_3__WEB03HD_17213313P_17243000P_Q8C.mp4"
      },
      {
        "Snooker-Staatsmeisterschaften in Graz",
        Duration.of(128, ChronoUnit.SECONDS),
        "Bei den Snooker-Staatsmeisterschaften in Graz hat Lokalmatador Florian Nüßle seinen Titel verteidigt. Und mit der EM in Bulgarien wartet auf Österreichs Herren Anfang Februar schon das nächste Highlight.",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/173c905e9868810a4eaac5521a267ca0213a6dd8.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/7faf90cc1aba7d01a26f9cb5885a86f5/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Snooker-Staatsm__13962830__o__8388588215__s14226904_4__WEB03HD_17243000P_17263821P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/43ed0fec340b265240e48e2d94feddf4/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Snooker-Staatsm__13962830__o__8388588215__s14226904_4__WEB03HD_17243000P_17263821P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/ea6df6a1d9a088b297813728a790c195/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Snooker-Staatsm__13962830__o__8388588215__s14226904_4__WEB03HD_17243000P_17263821P_Q8C.mp4"
      },
      {
        "24 Stunden Burgenland Extrem Tour",
        Duration.of(134, ChronoUnit.SECONDS),
        "Für jene, denen ein Marathon zu kurz ist, gibt es noch extremere Herausforderungen: 120 Kilometer zu Fuß rund um den Neusiedler See, und das in 24 Stunden - die \"24 Stunden Burgenland Extrem Tour\".",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/e910d5b0981b97b44777fbba91194cb12d3e890a.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/c52d1c55e401c4e5f3252e59ddd1097e/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_24-Stunden-Burg__13962830__o__2332651625__s14226905_5__WEB03HD_17263821P_17285221P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/394d058ac5456e6a83bb1f8dec9296e4/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_24-Stunden-Burg__13962830__o__2332651625__s14226905_5__WEB03HD_17263821P_17285221P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/6b186ef64c267758c08e03f4c409f497/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_24-Stunden-Burg__13962830__o__2332651625__s14226905_5__WEB03HD_17263821P_17285221P_Q8C.mp4"
      },
      {
        "Verabschiedung",
        Duration.of(26, ChronoUnit.SECONDS),
        "",
        "http://api-tvthek.orf.at/uploads/media/subtitles/0021/25/78463f837357241ee915d4a12ca41f023304f0e1.ttml",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/eb8e5b3de7982efb34aaba16c2f9eb51/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Verabschiedung__13962830__o__1244781515__s14226927_7__WEB03HD_17285221P_17291910P_Q4A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/0c1abe74d42a186915be9ae331f5ebce/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Verabschiedung__13962830__o__1244781515__s14226927_7__WEB03HD_17285221P_17291910P_Q6A.mp4",
        "http://localhost:8589/apasfpd.apa.at/cms-worldwide/online/960026cc81d733cf6d1dca429e304ce6/1517170556/2018-01-26_1705_tl_02_AKTUELL-IN-OEST_Verabschiedung__13962830__o__1244781515__s14226927_7__WEB03HD_17285221P_17291910P_Q8C.mp4"
      }
    };
  }
  
  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
    JsoupMock.mock(REQUEST_URL,
        "/orf/orf_film_with_several_parts.html");

    Object[][] films = data();
    
    final Set<Film> actual = executeTask(EXPECTED_THEME, REQUEST_URL);
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(films.length));
    
    actual.forEach(actualFilm -> {
      Object[] expectedData = getExpectedValues(films, actualFilm.getTitel());
      assertThat(expectedData, notNullValue());
      
      AssertFilm.assertEquals(actualFilm,
        Sender.ORF,
        EXPECTED_THEME,
        expectedData[INDEX_TITLE].toString(),
        EXPECTED_TIME,
        (Duration)expectedData[INDEX_DURATION],
        expectedData[INDEX_DESCRIPTION].toString(),
        REQUEST_URL,
        EXPECTED_GEO,
        expectedData[INDEX_URL_SMALL].toString(),
        expectedData[INDEX_URL_NORMAL].toString(),
        expectedData[INDEX_URL_HD].toString(),
        expectedData[INDEX_SUBTITLE].toString()
      );
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
