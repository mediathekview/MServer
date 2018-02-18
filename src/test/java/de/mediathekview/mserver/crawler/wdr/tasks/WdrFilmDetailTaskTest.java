package de.mediathekview.mserver.crawler.wdr.tasks;

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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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

class TestData {
  public String requestUrl;
  public String filmPageFile;
  public String jsUrl;
  public String jsFile;
  public String m3u8Url;
  public String m3u8File;
  public String topic;
  public String expectedTitle;
  public LocalDateTime expectedDate;
  public Duration expectedDuration;
  public String expectedDescription;
  public String expectedSubtitle;
  public String expectedUrlSmall;
  public String expectedUrlNormal;
  public String expectedUrlHd;
  public GeoLocations[] expectedGeoLocations;   

  public TestData(final String aRequestUrl,
    final String aFilmPageFile,
    final String aJsUrl,
    final String aJsFile,
    final String aM3u8Url,
    final String aM3u8File,
    final String aTopic,
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
    jsUrl = aJsUrl;
    jsFile = aJsFile;
    m3u8Url = aM3u8Url;
    m3u8File = aM3u8File;
    topic = aTopic;
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
}

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore("javax.net.ssl.*")
public class WdrFilmDetailTaskTest extends WdrTaskTestBase {
  
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {  
      {
        new TestData[] {
          new TestData(
            "http://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/video-die-tricks-des-ueberlebens--im-wald-102.html", 
            "/wdr/wdr_film1.html",
            "/ondemand/148/1480611.js", 
            "/wdr/wdr_video1.js",
            "/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/master.m3u8",
            "/wdr/wdr_video1.m3u8",
            "Abenteuer Erde",
            "Die Tricks des Überlebens 3) Im Wald", 
            LocalDateTime.of(2017, 9, 26, 20, 15, 0),
            Duration.ofMinutes(43).plusSeconds(20),
            "Nur auf der Nordhalbkugel gibt es Wälder, deren Leben durch große Veränderungen geprägt wird. Jedes Jahr lässt sich hier ein wundersamer Wechsel beobachten: im Winter sinken die Temperaturen dramatisch und die Wälder werden völlig kahl. Im Frühjahr kehren mit steigenden Temperaturen die grünen Blätter und damit das Leben zurück. Autor/-in: Paul Bradshaw", 
            "",
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_0_av.m3u8",
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_2_av.m3u8",
            "",
            new GeoLocations[] { GeoLocations.GEO_NONE }
          )
        }
      },
      {
        new TestData[] {
          new TestData(
            "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html", 
            "/wdr/wdr_film2.html", 
            "/ondemand/140/1407842.js", 
            "/wdr/wdr_video2.js",
            "/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8",
            "/wdr/wdr_video2.m3u8",
            "Ausgerechnet", 
            "Ausgerechnet - Schokolade", 
            LocalDateTime.of(2017, 7, 15, 16, 0, 0),
            Duration.ofMinutes(43).plusSeconds(35),
            "Knapp 25 Prozent der Bevölkerung verzehren Schokolade mehrmals in der Woche. Hinzu kommt gut ein weiteres Viertel, das Schokolade etwa einmal pro Woche genießt. Frauen greifen hier generell häufiger zu als Männer.", 
            "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml",
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_0_av.m3u8",
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_2_av.m3u8",
            "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_4_av.m3u8",
            new GeoLocations[] { GeoLocations.GEO_DE }
          )
        }
      },
      {
        new TestData[] {
          new TestData(
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
            new GeoLocations[] { GeoLocations.GEO_NONE }
          ),
          new TestData(
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
            new GeoLocations[] { GeoLocations.GEO_NONE }
          ),
          new TestData(
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
            new GeoLocations[] { GeoLocations.GEO_NONE }
          )
        }
      }
    });
  }
  
  private final TestData[] expectedFilms;
  
  public WdrFilmDetailTaskTest(TestData[] aExpectedFilms) {
    expectedFilms = aExpectedFilms;
  }
  
  @Test
  public void test() throws IOException {
    setupHeadRequestForFileSize();
    
    Map<String, String> urlMapping = new HashMap<>();
    for (TestData expected : expectedFilms) {
      urlMapping.put(expected.requestUrl, expected.filmPageFile);
      setupSuccessfulResponse(expected.jsUrl, expected.jsFile);
      setupSuccessfulResponse(expected.m3u8Url, expected.m3u8File);
    }
    JsoupMock.mock(urlMapping);

    final String topic = expectedFilms[0].topic;
    final String requestUrl = expectedFilms[0].requestUrl;
    
    final Set<Film> actual = new WdrFilmDetailTask(createCrawler(), createCrawlerUrlDto(topic, requestUrl)).invoke();
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedFilms.length));
    
    final Object[] actualArray = actual.toArray();
    for (int i = 0; i < actual.size(); i++) {
      Film actualFilm = (Film) actualArray[i];
      
      Optional<TestData> expectedFilmOptional = getTestData(actualFilm.getTitel());
      assertThat(expectedFilmOptional.isPresent(), equalTo(true));
      
      TestData expectedFilm = expectedFilmOptional.get();
      
      AssertFilm.assertEquals(actualFilm, 
        Sender.WDR,
        topic,
        expectedFilm.expectedTitle,
        expectedFilm.expectedDate,
        expectedFilm.expectedDuration,
        expectedFilm.expectedDescription,
        expectedFilm.requestUrl,
        expectedFilm.expectedGeoLocations,
        expectedFilm.expectedUrlSmall,
        expectedFilm.expectedUrlNormal,
        expectedFilm.expectedUrlHd,
        expectedFilm.expectedSubtitle
      );
    }
  }
  
  private Optional<TestData> getTestData(final String aTitle) {
    for (TestData expectedFilm : expectedFilms) {
      if (expectedFilm.expectedTitle.equalsIgnoreCase(aTitle)) {
        return Optional.of(expectedFilm);
      }
    }
    
    return Optional.empty();
  }
}
