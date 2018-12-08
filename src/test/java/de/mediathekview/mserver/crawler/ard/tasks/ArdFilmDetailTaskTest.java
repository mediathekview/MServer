package de.mediathekview.mserver.crawler.ard.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ArdFilmDetailTaskTest extends ArdTaskTestBase {
  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {
          "Y3JpZDovL21kci5kZS9iZWl0cmFnL2Ntcy8wMjc2NGZlMi0xNzIwLTQ1YzgtYmE1ZS00MzU3OTlmZDZlMDM",
            "/public_gateway?" + URLEncoder.encode("variables={\"client\":\"ard\",\"clipId\":\"Y3JpZDovL21kci5kZS9iZWl0cmFnL2Ntcy8wMjc2NGZlMi0xNzIwLTQ1YzgtYmE1ZS00MzU3OTlmZDZlMDM\",\"deviceType\":\"pc\"}&extensions={\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
            "/ard/ard_film_page1.json",
            "Sturm der Liebe",
            "Die schönsten Momente: Eva und Robert",
            LocalDateTime.of(2018, 12, 5, 15, 10, 0),
            Duration.ofMinutes(47).plusSeconds(36),
            "Dieses Special widmet sich der Liebesgeschichte von Eva und Robert. Es beleuchtet Roberts Trauerphase, aber auch die Rückkehr von Evas tot geglaubter erster großen Liebe Markus.",
            "https://www.ardmediathek.de/ard/player/Y3JpZDovL21kci5kZS9iZWl0cmFnL2Ntcy8wMjc2NGZlMi0xNzIwLTQ1YzgtYmE1ZS00MzU3OTlmZDZlMDM",
            "https://pdvideosdaserste-a.akamaihd.net/int/2018/12/05/c0c43211-2627-4a68-8757-be43c0dad75a/512-1.mp4",
            "https://pdvideosdaserste-a.akamaihd.net/int/2018/12/05/c0c43211-2627-4a68-8757-be43c0dad75a/960-1.mp4",
            "https://pdvideosdaserste-a.akamaihd.net/int/2018/12/05/c0c43211-2627-4a68-8757-be43c0dad75a/1280-1.mp4",
            "",
            GeoLocations.GEO_NONE
        }
    });
  }

  private String id;
  private final String filmUrl;
  private final String filmJsonFile;
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

  public ArdFilmDetailTaskTest(final String aId, final String aFilmUrl, final String aFilmJsonFile,
      final String aExpectedTopic, final String aExpectedTitle,
      final LocalDateTime aExpectedTime, final Duration aExpectedDuration, final String aExpectedDescription,
      final String aExpectedWebsite, final String aExpectedUrlSmall, final String aExpectedUrlNormal,
      final String aExpectedUrlHd, final String aExpectedSubtitle, final GeoLocations aExpectedGeo) {
    id = aId;
    filmUrl = aFilmUrl;
    filmJsonFile = aFilmJsonFile;
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

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);

    final Set<Film> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm
        .assertEquals(film, Sender.ARD, expectedTopic, expectedTitle, expectedTime, expectedDuration, expectedDescription, expectedWebsite,
            new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
  }

  private Set<Film> executeTask(final String aDetailUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new ArdFilmInfoDto(id, WireMockTestBase.MOCK_URL_BASE + aDetailUrl));
    return new ArdFilmDetailTask(createCrawler(), urls).invoke();
  }
}