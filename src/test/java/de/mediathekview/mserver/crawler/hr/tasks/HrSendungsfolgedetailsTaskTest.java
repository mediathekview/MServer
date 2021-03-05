package de.mediathekview.mserver.crawler.hr.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.HrCrawler;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
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

@RunWith(Parameterized.class)
public class HrSendungsfolgedetailsTaskTest extends HrTaskTestBase {

  private final String requestUrl;
  private final String htmlPage;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;

  public HrSendungsfolgedetailsTaskTest(
      final String aRequestUrl,
      final String aHtmlPage,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final String aExpectedDescription,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeo) {
    requestUrl = aRequestUrl;
    htmlPage = aHtmlPage;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://www.hr-fernsehen.de/sendungen-a-z/maintower-kriminalreport/sendungen/maintower-kriminalreport,sendung-62594.html",
            "/hr/hr_film_detail1.html",
            "maintower kriminalreport",
            "Missbrauch an der eigenen Tochter – Fahndung geht weiter",
            "Ein Mann prahlt im Internet mit dem Missbrauch einer 12-Jährigen und bietet das Mädchen an. Wie wahr ist diese Geschichte wirklich? Die weiteren Themen: Tödlicher Ausraster in Bad Soden | Ein Mörder wird gesucht | Wenn der Betrüger zweimal klingelt | Der Kleingarten als Tatort |Überfall auf Getränkemarkt in Gelnhausen-Haitz | Kleinkrieg am Gartenzaun",
            LocalDateTime.of(2019, 6, 2, 19, 0, 0),
            Duration.ofMinutes(29).plusSeconds(29),
            "https://hr-a.akamaihd.net/video/as/kriminalreport/2019_06/hrLogo_190602161651_0202702_512x288-25p-500kbit.mp4",
            "https://hr-a.akamaihd.net/video/as/kriminalreport/2019_06/hrLogo_190602161651_0202702_960x540-50p-1800kbit.mp4",
            "https://hr-a.akamaihd.net/video/as/kriminalreport/2019_06/hrLogo_190602161651_0202702_1280x720-50p-5000kbit.mp4",
            "https://hr-a.akamaihd.net/video/as/kriminalreport/2019_06/hrLogo_190602161651_0202702_512x288-25p-500kbit.vtt",
            GeoLocations.GEO_NONE
          },
          {
            "https://www.hr-fernsehen.de/sendungen-a-z/herrliches-hessen/sendungen/herrliches-hessen---unterwegs-in-und-um-eschenburg,sendung-37222.html",
            "/hr/hr_film_detail2.html",
            "herrliches hessen",
            "Unterwegs in und um Eschenburg",
            "Moderator Dieter Voss ist diesmal unterwegs im Lahn-Dill-Bergland – genauer gesagt in der Gemeinde Eschenburg. Die überwiegend ländlich geprägte Landschaft rund um Eschenburg ist ein echtes Paradies für Wander- und Naturfreunde.",
            LocalDateTime.of(2019, 6, 22, 0, 0, 0),
            Duration.ofMinutes(29).plusSeconds(27),
            "https://hr-a.akamaihd.net/video/as/herrlicheshessen/2019_06/hrLogo_190619163506_0202835_512x288-25p-500kbit.mp4",
            "https://hr-a.akamaihd.net/video/as/herrlicheshessen/2019_06/hrLogo_190619163506_0202835_960x540-50p-1800kbit.mp4",
            "https://hr-a.akamaihd.net/video/as/herrlicheshessen/2019_06/hrLogo_190619163506_0202835_1280x720-50p-5000kbit.mp4",
            "https://hr-a.akamaihd.net/video/as/herrlicheshessen/2019_06/hrLogo_190619163506_0202835_512x288-25p-500kbit.vtt",
            GeoLocations.GEO_NONE
          }
        });
  }

  @Test
  public void test() throws IOException {
    jsoupConnection = JsoupMock.mock(requestUrl, htmlPage);
    HrCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgedetailsTask target =
        new HrSendungsfolgedetailsTask(crawler, urls);
    final Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(
        actual.iterator().next(),
        Sender.HR,
        expectedTopic,
        expectedTitle,
        expectedTime,
        expectedDuration,
        expectedDescription,
        requestUrl,
        new GeoLocations[] {expectedGeo},
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitle);
  }
}
