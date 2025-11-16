package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.AssertFilm;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArdFilmDetailTaskTest extends ArdTaskTestBase {

  private final Map<String,String> urlStub;
  private final String crawlerUrl;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedWebsite;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedADUrlSmall;
  private final String expectedADUrlNormal;
  private final String expectedADUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  private final String id;
  private final Sender sender;
  
  public ArdFilmDetailTaskTest(
      final String aId,
      final String aCrawlerUrl,
      final Map<String,String> aUrlStub,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final String aExpectedDescription,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedADUrlSmall,
      final String aExpectedADUrlNormal,
      final String aExpectedADUrlHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeo,
      final String aExpectedWebsite,
      final Sender aSender) {
    id = aId;
    crawlerUrl = aCrawlerUrl;
    urlStub = aUrlStub;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedADUrlSmall = aExpectedADUrlSmall;
    expectedADUrlNormal = aExpectedADUrlNormal;
    expectedADUrlHd = aExpectedADUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
    sender = aSender;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            /* id */ "Y3JpZDovL2Z1bmsubmV0LzgzNS92aWRlby8xMDYzODE",
            /* crawlerUrl */ "/page-gateway/pages/ard/item/Y3JpZDovL2Z1bmsubmV0LzgzNS92aWRlby8xMDYzODE",
            /* stup*/ Map.ofEntries(
                Map.entry("/page-gateway/pages/ard/item/Y3JpZDovL2Z1bmsubmV0LzgzNS92aWRlby8xMDYzODE", "/ard/ard_item_fallback.json"),
                Map.entry("/22679/files/21/01/30/2678992/22679-jqh9gFKRm8YDnC2.ism/manifest.m3u8", "/ard/ard_item_fallback_m3u.txt")
            ),
            /*topic*/ "Fickt euch!",
            /*title*/ "Keine Chance für Smegma! Intimhygiene für Jungs I Fickt euch - Ist doch nur Sex",
            /*description*/ "Den Penis richtig waschen ist ganz einfach! Was ihr beachten müsst, um Infektionen und unangenehme Gerüche zu vermeiden, erfahrt ihr im Video. Du willst mehr? Dann abonniere meinen Kanal: https://www.youtube.com/channel/UC3ZkjIfabQzVypsQBd9-AIQ?sub_confirmation=1Fickt euch! bei Facebook: http://www.facebook.com/istdochnursexFickt euch! bei Instagram: http://www.instagram.com/istdochnursexFickt euc\n.....",
            /*date*/ LocalDateTime.parse("2016-12-13T15:00"),
            /*duration*/ Duration.parse("PT3M5S"),
            /*small*/ "http://localhost:50998/22679/files/21/01/30/2678992/22679-jqh9gFKRm8YDnC2.ism/22679-jqh9gFKRm8YDnC2-audio=152016-video=771000.m3u8",
            /*normal*/ "http://localhost:50998/22679/files/21/01/30/2678992/22679-jqh9gFKRm8YDnC2.ism/22679-jqh9gFKRm8YDnC2-audio=152016-video=2831000.m3u8",
            /*hd*/ "http://localhost:50998/22679/files/21/01/30/2678992/22679-jqh9gFKRm8YDnC2.ism/22679-jqh9gFKRm8YDnC2-audio=152016-video=3883000.m3u8",
            /*ADsmall*/ "",
            /*ADnormal*/ "",
            /*ADhd*/ "",
            /*sub*/ "",
            /*hd*/ GeoLocations.GEO_NONE,
            /* website */ "https://www.ardmediathek.de/video/Y3JpZDovL2Z1bmsubmV0LzgzNS92aWRlby8xMDYzODE",
            /* sender */ Sender.FUNK
          }
        });
  }

  @Test
  public void test() {
    
    for (Entry<String,String> entry : urlStub.entrySet()) {
      setupSuccessfulJsonResponse(entry.getKey(), entry.getValue());
    }

    final Set<Film> actual = executeTask(crawlerUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm.toTestCase(crawlerUrl, film);
    AssertFilm.assertEquals(
        film,
        sender,
        expectedTopic,
        expectedTitle,
        expectedTime,
        expectedDuration,
        expectedDescription,
        expectedWebsite,
        new GeoLocations[] {expectedGeo},
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        "","","", // sign language
        expectedADUrlSmall,
        expectedADUrlNormal,
        expectedADUrlHd,
        expectedSubtitle);
  }

  private Set<Film> executeTask(final String aDetailUrl) {
    final Queue<ArdFilmInfoDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new ArdFilmInfoDto(id, getWireMockBaseUrlSafe() + aDetailUrl, 0));
    return new ArdFilmDetailTask(createCrawler(), urls).invoke();
  }
}
