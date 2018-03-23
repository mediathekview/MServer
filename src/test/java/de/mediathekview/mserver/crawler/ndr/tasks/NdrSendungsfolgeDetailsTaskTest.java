package de.mediathekview.mserver.crawler.ndr.tasks;

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
import java.util.concurrent.ExecutionException;
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
public class NdrSendungsfolgeDetailsTaskTest extends NdrTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "https://www.ndr.de/fernsehen/sendungen/sass-so-isst-der-norden/Sass-So-isst-der-Norden,sendung563148.html",
                "/ndr/ndr_film_detail1.html",
                "/fernsehen/sendungen/sass-so-isst-der-norden/sass404-ardjson_image-5a3e2524-70e1-45f1-96da-27bf9d5c8137.json",
                "/ndr/ndr_film_detail1.json",
                "Sass: So isst der Norden",
                "Deftige Eintopfgerichte aus Bremen",
                "Rainer Sass macht mit seiner mobilen Küche Station auf dem Bremer Domshof. Mit seinen Kochpartnern vom dortigen Markt bereitet er Deftiges aus frischem Gemüse zu.",
                LocalDateTime.of(2018, 3, 18, 16, 30, 0),
                Duration.ofMinutes(30).plusSeconds(6),
                "https://mediandr-a.akamaihd.net/progressive/2016/1009/TV-20161009-1036-1800.hi.mp4",
                "https://mediandr-a.akamaihd.net/progressive/2016/1009/TV-20161009-1036-1800.hq.mp4",
                "https://mediandr-a.akamaihd.net/progressive/2016/1009/TV-20161009-1036-1800.hd.mp4",
                "",
                GeoLocations.GEO_NONE
            },
            {
                "https://www.ndr.de/fernsehen/Folge-2881-Der-langersehnte-Antrag,sturmderliebe1816.html",
                "/ndr/ndr_film_detail2.html",
                "/fernsehen/sturmderliebe1816-ardjson_image-a5409105-e38b-4847-ba32-fc2c337d7515.json",
                "/ndr/ndr_film_detail2.json",
                "Sturm der Liebe",
                "Folge 2881: Der langersehnte Antrag",
                "André fasst sich ein Herz und macht Melli den geplanten Antrag. Währenddessen ist Romy enttäuscht, dass Paul lediglich eine gute Freundin in ihr sieht.",
                LocalDateTime.of(2018, 3, 15, 8, 10, 0),
                Duration.ofMinutes(48).plusSeconds(34),
                "https://mediandr-a.akamaihd.net/progressive_geo/2018/0315/TV-20180315-0915-0800.hi.mp4",
                "https://mediandr-a.akamaihd.net/progressive_geo/2018/0315/TV-20180315-0915-0800.hq.mp4",
                "https://mediandr-a.akamaihd.net/progressive_geo/2018/0315/TV-20180315-0915-0800.hd.mp4",
                "https://www.ndr.de/media/ut65560.html",
                GeoLocations.GEO_DE
            }
        });
  }

  private final String requestUrl;
  private final String htmlPage;
  private final String jsonUrl;
  private final String jsonFile;
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

  public NdrSendungsfolgeDetailsTaskTest(final String aRequestUrl, final String aHtmlPage, final String aJsonUrl, final String aJsonFile,
      final String aExpectedTopic, final String aExpectedTitle, final String aExpectedDescription, final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration, final String aExpectedUrlSmall, final String aExpectedUrlNormal, final String aExpectedUrlHd,
      final String aExpectedSubtitle, final GeoLocations aExpectedGeo) {
    requestUrl = aRequestUrl;
    htmlPage = aHtmlPage;
    jsonUrl = aJsonUrl;
    jsonFile = aJsonFile;
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

  @Test
  public void test() throws IOException, ExecutionException, InterruptedException {
    JsoupMock.mock(requestUrl, htmlPage);
    setupSuccessfulJsonResponse(jsonUrl, jsonFile);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final NdrSendungsfolgedetailsTask target = new NdrSendungsfolgedetailsTask(createCrawler(), urls);
    final Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(actual.iterator().next(), Sender.NDR, expectedTopic, expectedTitle, expectedTime, expectedDuration,
        expectedDescription,
        requestUrl, new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }

}
