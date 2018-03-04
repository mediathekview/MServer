package de.mediathekview.mserver.crawler.rbb.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
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
public class RbbFilmTaskTest extends RbbTaskTestBase {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "http://mediathek.rbb-online.de/tv/Film-im-rbb/Zwei-Millionen-suchen-einen-Vater/rbb-Fernsehen/Video?bcastId=10009780&documentId=50543576",
                "/rbb/rbb_film_with_subtitle.html",
                "/play/media/50543576?devicetype=pc&features=hls",
                "/rbb/rbb_film_with_subtitle.json",
                "Film im Rbb",
                "Zwei Millionen suchen einen Vater",
                "Eine gewitzte Hotelinhaberin tut alles, um das Sorgerecht für ihren Schützling zu bekommen. ",
                LocalDateTime.of(2018, 3, 3, 14, 25, 0),
                Duration.ofHours(1).plusMinutes(28).plusSeconds(41),
                "https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_256k.mp4",
                "https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_1024k.mp4",
                "https://rbbmediapmdp-a.akamaihd.net/content/a0/93/a093d994-0ab0-498a-ae83-8e7647daa5db/3ccbfc08-3235-418f-9406-6c56cb89bb92_1800k.mp4",
                "http://mediathek.rbb-online.de/subtitle/217501",
                GeoLocations.GEO_NONE
            }
        });
  }

  private final String requestUrl;
  private final String htmlPage;
  private final String jsonUrl;
  private final String jsonFile;
  private final String topic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;

  public RbbFilmTaskTest(final String aRequestUrl, final String aHtmlPage, final String aJsonUrl, final String aJsonFile,
      final String aTopic, final String aExpectedTitle, final String aExpectedDescription, final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration, final String aExpectedUrlSmall, final String aExpectedUrlNormal, final String aExpectedUrlHd,
      final String aExpectedSubtitle, final GeoLocations aExpectedGeo) {
    requestUrl = aRequestUrl;
    htmlPage = aHtmlPage;
    jsonUrl = aJsonUrl;
    jsonFile = aJsonFile;
    topic = aTopic;
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

    final ConcurrentLinkedQueue<TopicUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new TopicUrlDTO(topic, requestUrl));

    final RbbFilmTask target = new RbbFilmTask(createCrawler(), urls);
    final Set<Film> actual = target.invoke();

    assertThat(actual.size(), equalTo(1));
    AssertFilm.assertEquals(actual.iterator().next(), Sender.RBB, topic, expectedTitle, expectedTime, expectedDuration, expectedDescription,
        requestUrl, new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }
}
