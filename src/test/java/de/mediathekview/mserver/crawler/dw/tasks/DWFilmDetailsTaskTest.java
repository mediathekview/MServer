package de.mediathekview.mserver.crawler.dw.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class DWFilmDetailsTaskTest extends DwTaskTestBase {

  private final String requestUrl;
  private final String htmlFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  private final String jsonRequestUrl;
  private final String jsonFile;

  public DWFilmDetailsTaskTest(
      final String requestUrl,
      final String htmlFile,
      final String jsonRequestUrl,
      final String jsonFile,
      final String expectedTopic,
      final String expectedTitle,
      final LocalDateTime expectedTime,
      final Duration expectedDuration,
      final String expectedDescription,
      final String expectedUrlSmall,
      final String expectedUrlNormal,
      final String expectedUrlHd,
      final String expectedSubtitle,
      final GeoLocations expectedGeo) {
    this.requestUrl = requestUrl;
    this.htmlFile = htmlFile;
    this.jsonRequestUrl = jsonRequestUrl;
    this.jsonFile = jsonFile;
    this.expectedTopic = expectedTopic;
    this.expectedTitle = expectedTitle;
    this.expectedTime = expectedTime;
    this.expectedDuration = expectedDuration;
    this.expectedDescription = expectedDescription;
    this.expectedUrlSmall = expectedUrlSmall;
    this.expectedUrlNormal = expectedUrlNormal;
    this.expectedUrlHd = expectedUrlHd;
    this.expectedSubtitle = expectedSubtitle;
    this.expectedGeo = expectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://www.dw.com/de/global-3000-das-globalisierungsmagazin/av-42986168",
            "/dw/dw_film_detail.html",
            "/playersources/v-42986168",
            "/dw/dw_film_detail.json",
            "Global 3000",
            "Global 3000 - Das Globalisierungsmagazin",
            LocalDateTime.of(2018, 10, 28, 0, 0, 0),
            Duration.ofMinutes(26).plusSeconds(1),
            "Manipulation von Videos, was ist noch echt? Die Waisenkinder von Mossul: wehrlose Opfer von Konflikten. Ein Comic-Heft verändert die Perspektiven von Kenias Jugend. Und für das Inselparadies Cozumel sind Kreuzfahrtschiffe Fluch und Segen zugleich.",
            "",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/gld/gld20180319_gesamt_sd_sor.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/gld/gld20180319_gesamt_sd_avc.mp4",
            "",
            GeoLocations.GEO_NONE
          },
          {
            "https://www.dw.com/de/shift-leben-in-der-digitalen-welt/av-56780778",
            "/dw/dw_film_detail2.html",
            "/playersources/v-56780778",
            "/dw/dw_film_detail2.json",
            "Shift",
            "Shift - Leben in der digitalen Welt",
            LocalDateTime.of(2021, 3, 9, 0, 0, 0),
            Duration.ofMinutes(12).plusSeconds(36),
            "Spezial: In Japan wird intensiv an neuen Technologien für die Katastrophenhilfe geforscht. Roboter helfen bei den Aufräumarbeiten im Atomkraftwerk Fukushima Daiichi und unterstützen Rettungskräfte nach Erdbeben.",
            "",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/shd/shd210305_SpezialJapanRoboter_sd_sor.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/shd/shd210305_SpezialJapanRoboter_sd_avc.mp4",
            "",
            GeoLocations.GEO_NONE
          }
        });
  }

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {

    final Connection connection = JsoupMock.mock(requestUrl, htmlFile);
    when(jsoupConnection.getConnection(requestUrl)).thenReturn(connection);

    setupSuccessfulJsonResponse(jsonRequestUrl, jsonFile);

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final DWFilmDetailsTask classUnderTest =
        new DWFilmDetailsTask(createCrawler(), urls, wireMockServer.baseUrl(), jsoupConnection);

    final Set<Film> actual = classUnderTest.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(
        actual.iterator().next(),
        Sender.DW,
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
