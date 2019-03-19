package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArteFilmTaskTest extends ArteTaskTestBase {

  private final Sender sender;
  private final String filmUrl;
  private final String filmJsonFile;
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

    public ArteFilmTaskTest(
            final Sender aSender,
            final String aFilmUrl,
            final String aFilmJsonFile,
            final String aVideoUrl,
            final String aVideoJsonFile,
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
            final GeoLocations aExpectedGeo) {
    this.sender = aSender;
    filmUrl = aFilmUrl;
    filmJsonFile = aFilmJsonFile;
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
                new Object[][]{
                        {
                                Sender.ARTE_DE,
                                "/api/opa/v3/programs/de/084733-002-A",
                                "/arte/arte_film_detail1.json",
                                "/api/player/v1/config/de/084733-002-A?platform=ARTE_NEXT",
                                "/arte/arte_film_video1.json",
                                "Kultur und Pop - Kunst",
                                "Stadt Land Kunst Spezial 2019 (2/26) - Bali",
                                "Diese Woche mit Fokus Bali: (1) Eat Pray Love. Erloschene Vulkane und grüne Reisterrassen so weit das Auge reicht: Bali ist der ideale Rahmen für eine Wiedergeburt! (2) Balis besonderes Korn: Das balinesische Herz schlägt in den Reisfeldern im Zentrum der Insel. (3) Das absolute Muss: Charlie Chaplins Indonesien voller Reisterrassen, Vulkane und buddhistischer Tempel.",
                                LocalDateTime.of(2019, 1, 12, 16, 35, 0),
                                Duration.ofMinutes(37).plusSeconds(51),
                                "https://www.arte.tv/de/videos/084733-002-A/stadt-land-kunst-spezial-2019-2-26/",
                                "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_HQ_0_VA-STA_04065289_MP4-800_AMM-PTWEB_1588D7RrM3.mp4",
                                "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_EQ_0_VA-STA_04065295_MP4-1500_AMM-PTWEB_1586Q7RqlB.mp4",
                                "https://arteptweb-a.akamaihd.net/am/ptweb/084000/084700/084733-002-A_SQ_0_VA-STA_04065288_MP4-2200_AMM-PTWEB_1588w7RraU.mp4",
                                "",
                                GeoLocations.GEO_DE_AT_CH_EU
                        }
                });
    }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);

    final Set<Film> actual = executeTask(filmUrl, videoUrl);

    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
      AssertFilm.assertEquals(
              film,
              sender,
              expectedTopic,
              expectedTitle,
              expectedTime,
              expectedDuration,
              expectedDescription,
              expectedWebsite,
              new GeoLocations[]{expectedGeo},
              expectedUrlSmall,
              expectedUrlNormal,
              expectedUrlHd,
              expectedSubtitle);
  }

    private Set<Film> executeTask(final String aDetailUrl, final String aVideoUrl) {
    final ConcurrentLinkedQueue<ArteFilmUrlDto> urls = new ConcurrentLinkedQueue<>();
        urls.add(
                new ArteFilmUrlDto(
                        WireMockTestBase.MOCK_URL_BASE + aDetailUrl,
                        WireMockTestBase.MOCK_URL_BASE + aVideoUrl));
        return new ArteFilmTask(createCrawler(), urls, sender, LocalDateTime.of(2019, 1, 12, 16, 0, 0))
                .invoke();
  }
}
