package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.testhelper.AssertFilm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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

public class ArdFilmDetailTaskTest extends ArdTaskTestBase {

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
            /*description*/ "Den Penis richtig waschen ist ganz einfach! Was ihr beachten müsst, um Infektionen und unangenehme Gerüche zu vermeiden, erfahrt ihr im Video. Du willst mehr? Dann abonniere meinen Kanal: https://www.youtube.com/channel/UC3ZkjIfabQzVypsQBd9-AIQ?sub_confirmation=1Fickt euch! bei Facebook: http://www.facebook.com/istdochnursexFickt euch! bei Instagram: http://www.instagram.com/istdochnursexFickt euch! bei Snapchat: @istdochnursex",
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

  @MethodSource("data")
  @ParameterizedTest
  void test(final String id, final String crawlerUrl, final Map<String, String> urlStub, final String expectedTopic, final String expectedTitle, final String expectedDescription, final LocalDateTime expectedTime, final Duration expectedDuration, final String expectedUrlSmall, final String expectedUrlNormal, final String expectedUrlHd, final String expectedADUrlSmall, final String expectedADUrlNormal, final String expectedADUrlHd, final String expectedSubtitle, final GeoLocations expectedGeo, final String expectedWebsite, final Sender sender) {

    for (Entry<String,String> entry : urlStub.entrySet()) {
      setupSuccessfulJsonResponse(entry.getKey(), entry.getValue());
    }

    final Set<Film> actual = executeTask(id, crawlerUrl);

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

  private Set<Film> executeTask(final String id, final String aDetailUrl) {
    final Queue<ArdFilmInfoDto> urls = new ConcurrentLinkedQueue<>();
    urls.add(new ArdFilmInfoDto(id, getWireMockBaseUrlSafe() + aDetailUrl, 0, false));
    return new ArdFilmDetailTask(createCrawler(), urls).invoke();
  }
}
