package de.mediathekview.mserver.crawler.dw.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsoupMock;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
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
import org.junit.runners.Parameterized.Parameters;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockRunnerDelegate(Parameterized.class)
@PowerMockIgnore("javax.net.ssl.*")
public class DWFilmDetailsTaskTest extends DwTaskTestBase {


  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
          "https://www.dw.com/de/global-3000-das-globalisierungsmagazin/av-42986168",
            "/dw/dw_film_detail.html",
            "/playersources/v-42986168",
            "/dw/dw_film_detail.json",
            "Global 3000",
            "Global 3000 - Das Globalisierungsmagazin",
            LocalDateTime.of(2018,10,28,0,0,0),
            Duration.ofMinutes(26).plusSeconds(1),
            "Manipulation von Videos, was ist noch echt? Die Waisenkinder von Mossul: wehrlose Opfer von Konflikten. Ein Comic-Heft verändert die Perspektiven von Kenias Jugend. Und für das Inselparadies Cozumel sind Kreuzfahrtschiffe Fluch und Segen zugleich.",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/gld/gld20180319_gesamt_sd_vp6.flv",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/gld/gld20180319_gesamt_sd_sor.mp4",
            "https://tvdownloaddw-a.akamaihd.net/dwtv_video/flv/gld/gld20180319_gesamt_sd_avc.mp4",
            "",
            GeoLocations.GEO_NONE
        }
    });
  }

  private final String requestUrl;
  private final String htmlFile;
  private String jsonRequestUrl;
  private String jsonFile;
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

  public DWFilmDetailsTaskTest(final String requestUrl, final String htmlFile, final String jsonRequestUrl, final String jsonFile, String expectedTopic, String expectedTitle,
      LocalDateTime expectedTime, Duration expectedDuration, String expectedDescription, String expectedUrlSmall,
      String expectedUrlNormal, String expectedUrlHd, String expectedSubtitle, GeoLocations expectedGeo) {
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

  @Test
  public void test() throws IOException {
    JsoupMock.mock(requestUrl, htmlFile);
    setupSuccessfulJsonResponse(jsonRequestUrl, jsonFile);

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    DWFilmDetailsTask target = new DWFilmDetailsTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE);
    Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(actual.iterator().next(), Sender.DW, expectedTopic, expectedTitle, expectedTime, expectedDuration,
        expectedDescription,
        requestUrl, new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }
}