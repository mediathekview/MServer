package de.mediathekview.mserver.crawler.phoenix.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.phoenix.PhoenixCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class PhoenixFilmDetailTaskTest extends WireMockTestBase {

  private final String filmUrl;
  private final String filmJsonFile;
  private final String filmDetailXmlUrl;
  private final String filmDetailXmlFile;
  private final String videoUrl;
  private final String videoJsonFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;
  private final String expectedDescription;
  private final String expectedWebsite;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  protected MServerConfigManager rootConfig =
      MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  public PhoenixFilmDetailTaskTest(
      final String aFilmUrl,
      final String aFilmJsonFile,
      final String aFilmDetailXmlUrl,
      final String aFilmDetailXmlFile,
      final String aVideoUrl,
      final String aVideoJsonFile,
      final String aExpectedTopic,
      final String aExpectedTitle,
      final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration,
      final String aExpectedDescription,
      final String aExpectedWebsite,
      final String aExpectedUrlSmall,
      final String aExpectedUrlNormal,
      final String aExpectedUrlHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeo) {
    filmUrl = aFilmUrl;
    filmJsonFile = aFilmJsonFile;
    filmDetailXmlUrl = aFilmDetailXmlUrl;
    filmDetailXmlFile = aFilmDetailXmlFile;
    videoUrl = aVideoUrl;
    videoJsonFile = aVideoJsonFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = aExpectedUrlSmall;
    expectedUrlNormal = aExpectedUrlNormal;
    expectedUrlHd = aExpectedUrlHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/response/id/271252",
            "/phoenix/phoenix_film_detail1.json",
            "/php/mediaplayer/data/beitrags_details.php?ak=web&ptmd=true&id=293872",
            "/phoenix/phoenix_film_detail1.xml",
            "/tmd/2/ngplayer_2_3/vod/ptmd/phoenix/180624_phx_presseclub",
            "/phoenix/phoenix_video_detail1.json",
            "Presseclub",
            "Mehr Grenzschutz und eine neue Asylpolitik – letzte Rettung für Europa und Merkel?",
            LocalDateTime.of(2018, 6, 24, 12, 0, 0),
            Duration.ofMinutes(57).plusSeconds(12),
            "Moderation: Sonia Seymour Mikich",
            "https://www.phoenix.de/sendungen/gespraeche/presseclub/mehr-grenzschutz-und-eine-neue-asylpolitik--letzte-rettung-fuer-europa-und-merkel-a-271252.html",
            "https://rodlzdf-a.akamaihd.net/none/phoenix/18/06/180624_phx_presseclub/1/180624_phx_presseclub_776k_p11v13.mp4",
            //"https://rodlzdf-a.akamaihd.net/none/phoenix/18/06/180624_phx_presseclub/1/180624_phx_presseclub_2328k_p35v13.mp4", // this video is not av. anymore
            "https://rodlzdf-a.akamaihd.net/none/phoenix/18/06/180624_phx_presseclub/1/180624_phx_presseclub_1496k_p13v13.mp4",
            "",
            "",
            GeoLocations.GEO_NONE
          }
        });
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulXmlResponse(filmDetailXmlUrl, filmDetailXmlFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);

    final Set<Film> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm.assertEquals(
        film,
        Sender.PHOENIX,
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
        expectedSubtitle);
  }

  protected PhoenixCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new PhoenixCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  private Set<Film> executeTask(final String aDetailUrl) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(wireMockServer.baseUrl() + aDetailUrl));
    return new PhoenixFilmDetailTask(
            createCrawler(), urls, null, wireMockServer.baseUrl(), wireMockServer.baseUrl())
        .invoke();
  }
}
