package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

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
public class WdrFilmDetailTaskTest extends WdrTaskTestBase {

  private final WdrFilmDetailTaskTestData[] expectedFilms;

  public WdrFilmDetailTaskTest(final WdrFilmDetailTaskTestData[] aExpectedFilms) {
    expectedFilms = aExpectedFilms;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            new WdrFilmDetailTaskTestData[] {
              new WdrFilmDetailTaskTestData(
                  "http://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/video-die-tricks-des-ueberlebens--im-wald-102.html",
                  "/wdr/wdr_film1.html",
                  "/ondemand/162/1626830.js",
                  "/wdr/wdr_video1.js",
                  "/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/master.m3u8",
                  "/wdr/wdr_video1.m3u8",
                  "Abenteuer Erde",
                  "Wilder Skagerrak",
                  LocalDateTime.of(2018, 4, 24, 20, 15, 0),
                  Duration.ofMinutes(44).plusSeconds(38),
                  "Der Skagerrak trennt den Süden Norwegens und Schwedens von Dänemark und ist eine Meerenge mit vielen Gesichtern. Wegen seiner kantigen schmalen Form wirkt er einengend wie ein Kanal auf die Wassermassen. Deshalb ist er einer der turbulentesten Meeresgebiete Europas. Autor/-in: Sigurd Tesche",
                  "",
                  "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_0_av.m3u8",
                  "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_2_av.m3u8",
                  "",
                  new GeoLocations[] {GeoLocations.GEO_NONE})
            }
          },
          {
            new WdrFilmDetailTaskTestData[] {
              new WdrFilmDetailTaskTestData(
                  "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html",
                  "/wdr/wdr_film2.html",
                  "/ondemand/140/1407842.js",
                  "/wdr/wdr_video2.js",
                  "/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8",
                  "/wdr/wdr_video2.m3u8",
                  "Ausgerechnet",
                  "Schokolade",
                  LocalDateTime.of(2017, 7, 15, 16, 0, 0),
                  Duration.ofMinutes(43).plusSeconds(35),
                  "Knapp 25 Prozent der Bevölkerung verzehren Schokolade mehrmals in der Woche. Hinzu kommt gut ein weiteres Viertel, das Schokolade etwa einmal pro Woche genießt. Frauen greifen hier generell häufiger zu als Männer.",
                  "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml",
                  "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_0_av.m3u8",
                  "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_2_av.m3u8",
                  "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_4_av.m3u8",
                  new GeoLocations[] {GeoLocations.GEO_DE})
            }
          },
          {
            new WdrFilmDetailTaskTestData[] {
              new WdrFilmDetailTaskTestData(
                  "http://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-wdr-aktuell-988.html",
                  "/wdr/wdr_film_with_part.html",
                  "/ondemand/158/1583472.js",
                  "/wdr/wdr_film_with_part.js",
                  "/i/medp/ondemand/weltweit/fsk0/158/1583472/,1583472_18232890,1583472_18232889,1583472_18232891,1583472_18232887,1583472_18232888,.mp4.csmil/master.m3u8",
                  "/wdr/wdr_film_with_part.m3u8",
                  "WDR aktuell",
                  "WDR aktuell",
                  LocalDateTime.of(2018, 2, 16, 21, 45, 0),
                  Duration.ofMinutes(15).plusSeconds(4),
                  "Moderiert von Martina Eßer",
                  "",
                  "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583472/,1583472_18232890,1583472_18232889,1583472_18232891,1583472_18232887,1583472_18232888,.mp4.csmil/index_0_av.m3u8",
                  "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583472/,1583472_18232890,1583472_18232889,1583472_18232891,1583472_18232887,1583472_18232888,.mp4.csmil/index_2_av.m3u8",
                  "",
                  new GeoLocations[] {GeoLocations.GEO_NONE}),
              new WdrFilmDetailTaskTestData(
                  "http://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-deniz-yuecel-ist-frei-102.html",
                  "/wdr/wdr_film_part1.html",
                  "/ondemand/158/1583853.js",
                  "/wdr/wdr_film_part1.js",
                  "/i/medp/ondemand/weltweit/fsk0/158/1583853/,1583853_18232860,1583853_18232858,1583853_18232859,1583853_18232856,1583853_18232857,.mp4.csmil/master.m3u8",
                  "/wdr/wdr_film_part1.m3u8",
                  "WDR aktuell",
                  "Deniz Yücel ist frei",
                  LocalDateTime.of(2018, 2, 16, 21, 45, 0),
                  Duration.ofMinutes(2).plusSeconds(1),
                  "Der Journalist Deniz Yücel ist frei. Ein Jahr lang saß er wegen Terrorvorwürfen in türkischer Haft. In den vergangenen Tagen kam Bewegung in den Fall, auch Außenminister Sigmar Gabriel hatte sich intensiv um eine Lösung bemüht. Offenbar mit Erfolg. Es gibt jetzt zwar eine Anklage, aber keine Ausreisesperre.",
                  "",
                  "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583853/,1583853_18232860,1583853_18232858,1583853_18232859,1583853_18232856,1583853_18232857,.mp4.csmil/index_0_av.m3u8",
                  "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583853/,1583853_18232860,1583853_18232858,1583853_18232859,1583853_18232856,1583853_18232857,.mp4.csmil/index_2_av.m3u8",
                  "",
                  new GeoLocations[] {GeoLocations.GEO_NONE}),
              new WdrFilmDetailTaskTestData(
                  "http://www1.wdr.de/mediathek/video/sendungen/wdr-aktuell/video-deutschlandtrend-und-jusos-no-groko-100.html",
                  "/wdr/wdr_film_part2.html",
                  "/ondemand/158/1583857.js",
                  "/wdr/wdr_film_part2.js",
                  "/i/medp/ondemand/weltweit/fsk0/158/1583472/,1583472_18232890,1583472_18232889,1583472_18232891,1583472_18232887,1583472_18232888,.mp4.csmil/master.m3u8",
                  "/wdr/wdr_film_part2.m3u8",
                  "WDR aktuell",
                  "Deutschlandtrend und Jusos No-GroKo",
                  LocalDateTime.of(2018, 2, 16, 21, 45, 0),
                  Duration.ofSeconds(0),
                  "",
                  "",
                  "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583472/,1583472_18232890,1583472_18232889,1583472_18232891,1583472_18232887,1583472_18232888,.mp4.csmil/index_0_av.m3u8",
                  "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583472/,1583472_18232890,1583472_18232889,1583472_18232891,1583472_18232887,1583472_18232888,.mp4.csmil/index_2_av.m3u8",
                  "",
                  new GeoLocations[] {GeoLocations.GEO_NONE})
            }
          }
        });
  }

  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();

    final Map<String, String> urlMapping = new HashMap<>();
    for (final WdrFilmDetailTaskTestData expected : expectedFilms) {
      urlMapping.put(expected.getRequestUrl(), expected.getFilmPageFile());
      setupSuccessfulResponse(expected.getJsUrl(), expected.getJsFile());
      setupSuccessfulResponse(expected.getM3u8Url(), expected.getM3u8File());
    }
    JsoupMock.mock(urlMapping);

    final String topic = expectedFilms[0].getTopic();
    final String requestUrl = expectedFilms[0].getRequestUrl();

    final Set<Film> actual =
        new WdrFilmDetailTask(createCrawler(), createCrawlerUrlDto(topic, requestUrl)).invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedFilms.length));

    final Object[] actualArray = actual.toArray();
    for (int i = 0; i < actual.size(); i++) {
      final Film actualFilm = (Film) actualArray[i];

      final Optional<WdrFilmDetailTaskTestData> expectedFilmOptional =
          getTestData(actualFilm.getTitel());
      assertThat(expectedFilmOptional.isPresent(), equalTo(true));

      final WdrFilmDetailTaskTestData expectedFilm = expectedFilmOptional.get();

      AssertFilm.assertEquals(
          actualFilm,
          Sender.WDR,
          topic,
          expectedFilm.getExpectedTitle(),
          expectedFilm.getExpectedDate(),
          expectedFilm.getExpectedDuration(),
          expectedFilm.getExpectedDescription(),
          expectedFilm.getRequestUrl(),
          expectedFilm.getExpectedGeoLocations(),
          expectedFilm.getExpectedUrlSmall(),
          expectedFilm.getExpectedUrlNormal(),
          expectedFilm.getExpectedUrlHd(),
          expectedFilm.getExpectedSubtitle());
    }
  }

  private Optional<WdrFilmDetailTaskTestData> getTestData(final String aTitle) {
    for (final WdrFilmDetailTaskTestData expectedFilm : expectedFilms) {
      if (expectedFilm.getExpectedTitle().equalsIgnoreCase(aTitle)) {
        return Optional.of(expectedFilm);
      }
    }

    return Optional.empty();
  }
}
