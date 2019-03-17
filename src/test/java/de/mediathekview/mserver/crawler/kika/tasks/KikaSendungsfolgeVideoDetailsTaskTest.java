package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
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
public class KikaSendungsfolgeVideoDetailsTaskTest extends KikaTaskTestBase {

  private String requestUrl;
  private String xmlFile;
  private String expectedTopic;
  private String expectedTitle;
  private String expectedDescription;
  private LocalDateTime expectedTime;
  private Duration expectedDuration;
  private String expectedWebsite;
  private String expectedUrlSmall;
  private String expectedUrlNormal;
  private String expectedUrlHd;
  private String expectedSubtitle;
  private GeoLocations expectedGeoLocation;

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "https://www.kika.de/rocket-ich/sendungen/videos/video14406-avCustom.xml",
                "/kika/kika_film_video1.xml",
                "Rocket & Ich",
                "38. Auf den Klon gekommen",
                "Vinnies Wunsch zur Schickimickeria zu gehören wird scheinbar nicht erhört, denn alle außer ihm erhalten eine Einladung zu Kapitän Goldzahns Promi-Piraten-Party. Sein Freund Rainbow verzichtet zugunsten von Vinnie auf die Party und verabredet sich mit ihm zum Turbo-Tubenkäse-Draufmachathon. Kurz darauf erhält Vinnie doch noch die ersehnte Einladung zur Party. Nun muss er sich entscheiden.",
                LocalDateTime.of(2019, 3, 10, 7, 0, 0),
                Duration.ofMinutes(11).plusSeconds(54),
                "https://www.kika.de/rocket-ich/sendungen/sendung41184.html",
                "https://pmdonlinekika-a.akamaihd.net/mp4dyn/1/FCMS-12121f14-dce7-4cf4-a928-affa5a312ce7-2cc6c1c1f632_12.mp4",
                "https://pmdonlinekika-a.akamaihd.net/mp4dyn/1/FCMS-12121f14-dce7-4cf4-a928-affa5a312ce7-31e0be270130_12.mp4",
                "https://pmdonlinekika-a.akamaihd.net/mp4dyn/1/FCMS-12121f14-dce7-4cf4-a928-affa5a312ce7-5a2c8da1cdb7_12.mp4",
                "",
                GeoLocations.GEO_NONE
            },
            {
                "https://www.kika.de/mama-fuchs-und-papa-dachs/sendungen/videos/video66904-avCustom.xml",
                "/kika/kika_film_video2.xml",
                "Mama Fuchs und Papa Dachs",
                "17. Der Teddy",
                "Matteos Teddy Theodor ist verschwunden. Niemand weiß von seinem geheimen Freund und das soll auch so bleiben. Aber es ist gar nicht so einfach nach etwas zu fragen und nicht zu verraten, was es ist. Können die Geschwister zusammen den Teddy finden?",
                LocalDateTime.of(2019, 3, 17, 6, 35, 0),
                Duration.ofMinutes(12).plusSeconds(4),
                "https://www.kika.de/mama-fuchs-und-papa-dachs/sendungen/sendung111180.html",
                "https://pmdgeokika-a.akamaihd.net/mp4dyn/f/FCMS-f6246737-fa13-4990-b801-2706fb13c1b6-2cc6c1c1f632_f6.mp4",
                "https://pmdgeokika-a.akamaihd.net/mp4dyn/f/FCMS-f6246737-fa13-4990-b801-2706fb13c1b6-31e0be270130_f6.mp4",
                "https://pmdgeokika-a.akamaihd.net/mp4dyn/f/FCMS-f6246737-fa13-4990-b801-2706fb13c1b6-5a2c8da1cdb7_f6.mp4",
                "",
                GeoLocations.GEO_DE
            }
        });
  }

  public KikaSendungsfolgeVideoDetailsTaskTest(final String aRequestUrl, final String aXmlFile,
      final String aExpectedTopic, final String aExpectedTitle, final String aExpectedDescription,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration,
      final String aExpectedWebsite, final String aExpectedUrlSmall, final String aExpectedUrlNormal, final String aExpectedUrlHd,
      final String aExpectedSubtitle, GeoLocations aExpectedGeoLocation) {

    requestUrl = aRequestUrl;
    xmlFile = aXmlFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeoLocation = aExpectedGeoLocation;
  }

  @Test
  public void test() throws IOException {
    JsoupMock.mockXml(requestUrl, xmlFile);

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaSendungsfolgeVideoDetailsTask target = new KikaSendungsfolgeVideoDetailsTask(createCrawler(), urls);
    Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(actual.iterator().next(), Sender.KIKA, expectedTopic, expectedTitle, expectedTime, expectedDuration,
        expectedDescription,
        expectedWebsite, new GeoLocations[]{expectedGeoLocation}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);

  }
}