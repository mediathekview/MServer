package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class KikaSendungsfolgeVideoDetailsTaskTest extends KikaTaskTestBase {

  private final String requestUrl;
  private final String xmlFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedWebsite;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeoLocation;

  public KikaSendungsfolgeVideoDetailsTaskTest(
      final String aRequestUrl,
      final String aXmlFile,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final String aExpectedDescription,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedWebsite,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeoLocation) {

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

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
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
          },
          {
            "https://www.kika.de/gut-gebruellt-liebe-monster/sendungen/videos/misch-masch-salat-108-avCustom.xml",
            "/kika/kika_film_video_noresolution.xml",
            "Gut gebrüllt, liebe Monster!",
            "42. Misch-Masch-Salat",
            "Diesmal haben die kleinen Monster Gemüse von daheim mitgebracht und bereiten daraus einen Salat zu. Weil Schnuffelplumps die Schatz-omaten darin nicht mag, versteckt er die Salatschüssel.",
            LocalDateTime.of(2020, 10, 12, 8, 10, 0),
            Duration.ofMinutes(7),
            "https://www.kika.de/gut-gebruellt-liebe-monster/sendungen/sendung124430.html",
            "https://nrodlzdf-a.akamaihd.net/de/tivi/20/08/200805_folge42_mischmaschsalat_gut/2/200805_folge42_mischmaschsalat_gut_508k_p9v15.mp4",
            "https://nrodlzdf-a.akamaihd.net/de/tivi/20/08/200805_folge42_mischmaschsalat_gut/2/200805_folge42_mischmaschsalat_gut_808k_p11v15.mp4",
            "https://nrodlzdf-a.akamaihd.net/de/tivi/20/08/200805_folge42_mischmaschsalat_gut/2/200805_folge42_mischmaschsalat_gut_1628k_p13v15.mp4",
            "",
            GeoLocations.GEO_NONE
          },
          {
            "https://www.kika.de/av-import/ohne-sendungsbezug/felix-sucht-bakterien100-avCustom.xml",
            "/kika/kika_film_video3.xml",
            "ERDE AN ZUKUNFT",
            "Gefährliche Keime - wie schützen wir uns in Zukunft?",
            "Immer mehr Keime machen uns krank, vor allem Bakterien und Viren. Sie sind winzig, aber trotzdem stark genug, Menschen schwer krank zu machen. Gegen manche dieser Bakterien und Viren hilft noch keines der Medikamente, die wir heute kennen. Wie können wir uns vor solchen Super-Keimen schützen?",
            LocalDateTime.of(2020, 7, 12, 8, 0, 0),
            Duration.ofMinutes(3).plusSeconds(59),
            "https://www.kika.de/erde-an-zukunft/sendungen/sendung121534.html",
            "https://pmdonlinekika-a.akamaihd.net/mp4dyn/b/FCMS-bc273f47-0dee-4577-86db-e74f64797421-2cc6c1c1f632_bc.mp4",
            "https://pmdonlinekika-a.akamaihd.net/mp4dyn/b/FCMS-bc273f47-0dee-4577-86db-e74f64797421-31e0be270130_bc.mp4",
            "https://pmdonlinekika-a.akamaihd.net/mp4dyn/b/FCMS-bc273f47-0dee-4577-86db-e74f64797421-5a2c8da1cdb7_bc.mp4",
            "",
            GeoLocations.GEO_NONE
          }
        });
  }

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Mock JsoupConnection jsoupConnection;

  @Test
  public void test() throws IOException {
    final Document document = JsoupMock.mockXml(requestUrl, xmlFile);
    when(jsoupConnection.getDocumentTimeoutAfterAlternativeDocumentType(
            eq(requestUrl), anyInt(), any()))
        .thenReturn(document);

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaSendungsfolgeVideoDetailsTask target =
        new KikaSendungsfolgeVideoDetailsTask(createCrawler(), urls);
    target.setJsoupConnection(jsoupConnection);
    final Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(
        actual.iterator().next(),
        Sender.KIKA,
        expectedTopic,
        expectedTitle,
        expectedTime,
        expectedDuration,
        expectedDescription,
        expectedWebsite,
        new GeoLocations[] {expectedGeoLocation},
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitle);
  }
}
