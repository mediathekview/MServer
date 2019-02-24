package de.mediathekview.mserver.crawler.wdr.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.tasks.WdrTaskTestBase;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.FileReader;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@Ignore(value="The wrd URLs aren't avialable!")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore(value= {"javax.net.ssl.*", "javax.*", "com.sun.*", "org.apache.logging.log4j.core.config.xml.*"})
public class WdrFilmDeserializerTest extends WdrTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "http://www1.wdr.de/mediathek/video/sendungen/abenteuer-erde/video-die-tricks-des-ueberlebens--im-wald-102.html",
                "/wdr/wdr_film1.html",
                "/ondemand/162/1626830.js",
                "/wdr/wdr_video1.js",
                "/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/master.m3u8",
                "/wdr/wdr_video1.m3u8",
                "",
                "",
                "",
                "",
                "Abenteuer Erde",
                "Wilder Skagerrak",
                LocalDateTime.of(2018, 4, 24, 20, 15, 0),
                Duration.ofMinutes(44).plusSeconds(38),
                "Der Skagerrak trennt den Süden Norwegens und Schwedens von Dänemark und ist eine Meerenge mit vielen Gesichtern. Wegen seiner kantigen schmalen Form wirkt er einengend wie ein Kanal auf die Wassermassen. Deshalb ist er einer der turbulentesten Meeresgebiete Europas. Autor/-in: Sigurd Tesche",
                "",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_0_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/148/1480611/,1480611_16974214,1480611_16974213,1480611_16974215,1480611_16974211,1480611_16974212,.mp4.csmil/index_2_av.m3u8",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new GeoLocations[]{GeoLocations.GEO_NONE}
            },
            {
                "http://www1.wdr.de/mediathek/video/sendungen/ausgerechnet/video-ausgerechnet---schokolade-102.html",
                "/wdr/wdr_film2.html",
                "/ondemand/140/1407842.js",
                "/wdr/wdr_video2.js",
                "/i/medp/ondemand/weltweit/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/master.m3u8",
                "/wdr/wdr_video2.m3u8",
                "",
                "",
                "",
                "",
                "Ausgerechnet",
                "Schokolade",
                LocalDateTime.of(2017, 7, 15, 16, 0, 0),
                Duration.ofMinutes(43).plusSeconds(35),
                "Knapp 25 Prozent der Bevölkerung verzehren Schokolade mehrmals in der Woche. Hinzu kommt gut ein weiteres Viertel, das Schokolade etwa einmal pro Woche genießt. Frauen greifen hier generell häufiger zu als Männer.",
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/140/1407842/1407842_16348809.xml",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_0_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_2_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/de/fsk0/140/1407842/,1407842_16309723,1407842_16309728,1407842_16309725,1407842_16309726,1407842_16309724,1407842_16309727,.mp4.csmil/index_4_av.m3u8",
                "",
                "",
                "",
                "",
                "",
                "",
                new GeoLocations[]{GeoLocations.GEO_DE}
            },
            {
                "http://www1.wdr.de/mediathek/video/sendungen/quarks-und-co/video-bin-ich-schneller-als-ein-tyrannosaurus-rex-102.html",
                "/wdr/wdr_film3.html",
                "/ondemand/47/476693.js",
                "/wdr/wdr_video_v1_1.js",
                "",
                "",
                "",
                "",
                "",
                "",
                "Quarks & Co",
                "Bin ich schneller als ein Tyrannosaurus rex?",
                LocalDateTime.of(2016, 2, 23, 21, 0, 0),
                Duration.ofMinutes(1).plusSeconds(45),
                "Ein Zusammentreffen zwischen Dinosauriern wie Tyrannosaurus rex und dem Menschen gibt es nur im Kino. Aber was wäre wenn? Wären Sie schnell genug, um weglaufen zu können?",
                "",
                "",
                "http://ondemand-ww.wdr.de/medp/fsk0/47/476693/476693_12040646.mp4",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                new GeoLocations[]{GeoLocations.GEO_NONE}
            },
            {
                "http://www1.wdr.de/kinder/tv/die-sendung-mit-der-maus/av/video-die-sendung-mit-der-maus-vom--598.html",
                "/wdr/wdr_film_with_ad_dgs.html",
                "/ondemand/162/1629126.js",
                "/wdr/wdr_video_with_ad_dgs.js",
                "/i/medp/ondemand/weltweit/fsk0/162/1629126/,1629126_18799707,1629126_18799719,1629126_18799712,1629126_18799713,1629126_18799717,1629126_18799722,.mp4.csmil/master.m3u8",
                "/wdr/wdr_video_with_ad_dgs1.m3u8",
                "/i/medp/ondemand/weltweit/fsk0/162/1629126/,1629126_18799706,1629126_18799718,1629126_18799710,1629126_18799711,1629126_18799716,1629126_18799721,.mp4.csmil/master.m3u8",
                "/wdr/wdr_video_with_ad_dgs2.m3u8",
                "/i/medp/ondemand/weltweit/fsk0/162/1629126/,1629126_18799705,1629126_18799714,1629126_18799708,1629126_18799709,1629126_18799715,1629126_18799720,.mp4.csmil/master.m3u8",
                "/wdr/wdr_video_with_ad_dgs3.m3u8",
                "Die Sendung mit der Maus",
                "vom 22.04.2018",
                LocalDateTime.of(2018, 4, 22, 9, 30, 1),
                Duration.ofMinutes(30),
                "Lach- und Sachgeschichten mit Armins Rohr-Test, einem Ausflug in die Natur, dem Geheimnis der Pusteblume, einer sehr hungrigen Ziege und mit der Maus und dem Elefanten.",
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/162/1629126/1629126_18799781.xml",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232361,1583693_18232358,1583693_18232363,1583693_18232362,1583693_18232359,1583693_18232360,.mp4.csmil/index_0_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232361,1583693_18232358,1583693_18232363,1583693_18232362,1583693_18232359,1583693_18232360,.mp4.csmil/index_2_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232361,1583693_18232358,1583693_18232363,1583693_18232362,1583693_18232359,1583693_18232360,.mp4.csmil/index_4_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232391,1583693_18232389,1583693_18232393,1583693_18232392,1583693_18232388,1583693_18232390,.mp4.csmil/index_0_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232391,1583693_18232389,1583693_18232393,1583693_18232392,1583693_18232388,1583693_18232390,.mp4.csmil/index_2_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232391,1583693_18232389,1583693_18232393,1583693_18232392,1583693_18232388,1583693_18232390,.mp4.csmil/index_4_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232344,1583693_18232347,1583693_18232346,1583693_18232345,1583693_18232343,1583693_18232342,.mp4.csmil/index_0_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232344,1583693_18232347,1583693_18232346,1583693_18232345,1583693_18232343,1583693_18232342,.mp4.csmil/index_2_av.m3u8",
                "http://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/158/1583693/,1583693_18232344,1583693_18232347,1583693_18232346,1583693_18232345,1583693_18232343,1583693_18232342,.mp4.csmil/index_4_av.m3u8",
                new GeoLocations[]{GeoLocations.GEO_NONE}
            },
            {
                "http://www1.wdr.de/mediathek/video/sendungen/video-henker--richter---die-sau-ist-tot--100.html",
                "/wdr/wdr_video_details5.html",
                "/ondemand/160/1607412.js",
                "/wdr/wdr_video5.js",
                "/i/medp/ondemand/weltweit/fsk0/160/1607412/,1607412_18526624,1607412_18526625,1607412_18526622,1607412_18526621,1607412_18526626,1607412_18526623,.mp4.csmil/master.m3u8",
                "/wdr/wdr_video5.m3u8",
                "",
                "",
                "",
                "",
                "Henker & Richter",
                "Die Sau ist tot (2/16)",
                LocalDateTime.of(2018, 3, 30, 16, 25, 0),
                Duration.ofMinutes(46).plusSeconds(47),
                "Zwei Hasen, ein paar Fasane und ein toter Bauer",
                "",
                "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/160/1607412/,1607412_18526624,1607412_18526625,1607412_18526622,1607412_18526621,1607412_18526626,1607412_18526623,.mp4.csmil/index_0_av.m3u8",
                "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/160/1607412/,1607412_18526624,1607412_18526625,1607412_18526622,1607412_18526621,1607412_18526626,1607412_18526623,.mp4.csmil/index_2_av.m3u8",
                "https://wdradaptiv-vh.akamaihd.net/i/medp/ondemand/weltweit/fsk0/160/1607412/,1607412_18526624,1607412_18526625,1607412_18526622,1607412_18526621,1607412_18526626,1607412_18526623,.mp4.csmil/index_4_av.m3u8",
                "",
                "",
                "",
                "",
                "",
                "",
                new GeoLocations[]{GeoLocations.GEO_NONE}
            }
        });
  }

  private final String requestUrl;
  private final String filmPageFile;
  private final String jsUrl;
  private final String jsFile;
  private final String m3u8UrlVideo;
  private final String m3u8FileVideo;
  private final String m3u8UrlSignLanguage;
  private final String m3u8FileSignLanguage;
  private final String m3u8UrlAudioDescription;
  private final String m3u8FileAudioDescription;
  private final String topic;
  private final String expectedTitle;
  private final LocalDateTime expectedDate;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedSubtitle;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedUrlSignLanguageSmall;
  private final String expectedUrlSignLanguageNormal;
  private final String expectedUrlSignLanguageHd;
  private final String expectedUrlAudioDescriptionSmall;
  private final String expectedUrlAudioDescriptionNormal;
  private final String expectedUrlAudioDescriptionHd;
  private final GeoLocations[] expectedGeoLocations;

  public WdrFilmDeserializerTest(
      final String aRequestUrl,
      final String aFilmPageFile,
      final String aJsUrl,
      final String aJsFile,
      final String aM3u8UrlVideo,
      final String aM3u8FileVideo,
      final String aM3u8UrlSignLanguage,
      final String aM3u8FileSignLanguage,
      final String aM3u8UrlAudioDescription,
      final String aM3u8FileAudioDescription,
      final String aTopic,
      final String aExpectedTitle,
      final LocalDateTime aExpectedDate,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedSubtitle,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedUrlSignLanguageSmall,
      final String aExpectedUrlSignLanguageNormal,
      final String aExpectedUrlSignLanguageHd,
      final String aExpectedUrlAudioDescriptionSmall,
      final String aExpectedUrlAudioDescriptionNormal,
      final String aExpectedUrlAudioDescriptionHd,
      final GeoLocations[] aExpectedGeoLocations) {
    requestUrl = aRequestUrl;
    filmPageFile = aFilmPageFile;
    jsUrl = aJsUrl;
    jsFile = aJsFile;
    m3u8UrlVideo = aM3u8UrlVideo;
    m3u8FileVideo = aM3u8FileVideo;
    m3u8UrlSignLanguage = aM3u8UrlSignLanguage;
    m3u8FileSignLanguage = aM3u8FileSignLanguage;
    m3u8UrlAudioDescription = aM3u8UrlAudioDescription;
    m3u8FileAudioDescription = aM3u8FileAudioDescription;
    topic = aTopic;
    expectedTitle = aExpectedTitle;
    expectedDate = aExpectedDate;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedSubtitle = aExpectedSubtitle;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedUrlSignLanguageSmall = aExpectedUrlSignLanguageSmall;
    expectedUrlSignLanguageNormal = aExpectedUrlSignLanguageNormal;
    expectedUrlSignLanguageHd = aExpectedUrlSignLanguageHd;
    expectedUrlAudioDescriptionSmall = aExpectedUrlAudioDescriptionSmall;
    expectedUrlAudioDescriptionNormal = aExpectedUrlAudioDescriptionNormal;
    expectedUrlAudioDescriptionHd = aExpectedUrlAudioDescriptionHd;
    expectedGeoLocations = aExpectedGeoLocations;
  }

  @Test
  public void test() throws IOException {
    final Document document = Jsoup.parse(FileReader.readFile(filmPageFile));
    setupSuccessfulResponse(jsUrl, jsFile);
    if (!m3u8UrlVideo.isEmpty()) {
      setupSuccessfulResponse(m3u8UrlVideo, m3u8FileVideo);
    }
    if (!m3u8UrlSignLanguage.isEmpty()) {
      setupSuccessfulResponse(m3u8UrlSignLanguage, m3u8FileSignLanguage);
    }
    if (!m3u8UrlAudioDescription.isEmpty()) {
      setupSuccessfulResponse(m3u8UrlAudioDescription, m3u8FileAudioDescription);
    }

    Optional<Film> actual =
        new WdrFilmDeserializer("http:", Sender.WDR)
            .deserialize(new TopicUrlDTO(topic, requestUrl), document);

    assertThat(actual.isPresent(), equalTo(true));

    Film actualFilm = actual.get();
    AssertFilm.assertEquals(
        actualFilm,
        Sender.WDR,
        topic,
        expectedTitle,
        expectedDate,
        expectedDuration,
        expectedDescription,
        requestUrl,
        expectedGeoLocations,
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
}
