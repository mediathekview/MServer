package de.mediathekview.mserver.crawler.arte.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class ArteFilmTaskTest extends ArteTaskTestBase {

  private final int exceptedFilmCount;
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
  private final String expectedUrlWithSubtitleSmall;
  private final String expectedUrlWithSubtitleNormal;
  private final String expectedUrlWithSubtitleHd;
  private final String expectedUrlAudioDescSmall;
  private final String expectedUrlAudioDescNormal;
  private final String expectedUrlAudioDescHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;

  public ArteFilmTaskTest(
      final int aExceptedFilmCount,
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
      final String aExpectedUrlWithSubtitleSmall,
      final String aExpectedUrlWithSubtitleNormal,
      final String aExpectedUrlWithSubtitleHd,
      final String aExpectedUrlAudioDescSmall,
      final String aExpectedUrlAudioDescNormal,
      final String aExpectedUrlAudioDescHd,
      final String aExpectedSubtitle,
      final GeoLocations aExpectedGeo) {
    exceptedFilmCount = aExceptedFilmCount;
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
    this.expectedUrlWithSubtitleSmall = aExpectedUrlWithSubtitleSmall;
    this.expectedUrlWithSubtitleNormal = aExpectedUrlWithSubtitleNormal;
    this.expectedUrlWithSubtitleHd = aExpectedUrlWithSubtitleHd;
    this.expectedUrlAudioDescSmall = aExpectedUrlAudioDescSmall;
    this.expectedUrlAudioDescNormal = aExpectedUrlAudioDescNormal;
    this.expectedUrlAudioDescHd = aExpectedUrlAudioDescHd;
    expectedSubtitle = aExpectedSubtitle;
    expectedGeo = aExpectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                1,
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
                "", "", "", "", "", "",
                "",
                GeoLocations.GEO_DE_AT_CH_EU
            },
            {
                3,
                Sender.ARTE_DE,
                "/api/opa/v3/programs/de/085744-084-A",
                "/arte/arte_film_detail2.json",
                "/api/player/v1/config/de/085744-084-A?platform=ARTE_NEXT",
                "/arte/arte_film_video_audio_desc_de.json",
                "Aktuelles und Gesellschaft - Junior",
                "ARTE Journal Junior",
                "Alle 10- bis 14-jährigen ARTE-Zuschauer können sich auf ihre werktägliche, sechsminütige Nachrichtensendung freuen! Carolyn Höfchen, Magali Kreuzer, Dorothée Haffner und Frank Rauschendorf moderieren und informieren wissbegierige Kids kurz und prägnant über alles, was in der Welt los ist.",
                LocalDateTime.of(2019, 4, 26, 7, 10, 0),
                Duration.ofMinutes(6).plusSeconds(7),
                "https://www.arte.tv/de/videos/085744-084-A/arte-journal-junior/",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VA-STA_04530699_MP4-800_AMM-PTWEB_1D78dzrOJf.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VA-STA_04530700_MP4-1500_AMM-PTWEB_1D6xwzrJ81.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VA-STA_04530698_MP4-2200_AMM-PTWEB_1D78KzrOD5.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VA-STMA_04530703_MP4-800_AMM-PTWEB_1D8uFzvxvH.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VA-STMA_04530704_MP4-1500_AMM-PTWEB_1D6y6zrJEN.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VA-STMA_04530702_MP4-2200_AMM-PTWEB_1D79WzrOmR.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_HQ_0_VAAUD_04530707_MP4-800_AMM-PTWEB_1D78ezrOJf.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_EQ_0_VAAUD_04530708_MP4-1500_AMM-PTWEB_1D6yCzrJI1.mp4",
                "https://arteptweb-a.akamaihd.net/am/ptweb/043000/043100/043187-000-B_SQ_0_VAAUD_04530706_MP4-2200_AMM-PTWEB_1D78azrOGD.mp4",
                "",
                GeoLocations.GEO_NONE
            }
        });
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);

    final Set<Film> actual = executeTask(filmUrl, videoUrl);

    assertThat(actual.size(), equalTo(exceptedFilmCount));

    Iterator<Film> iterator = actual.stream().sorted(Comparator.comparing(Film::getTitel))
        .iterator();
    Film film = iterator.next();
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

    if (exceptedFilmCount > 1) {
      film = iterator.next();
      AssertFilm.assertEquals(
          film,
          sender,
          expectedTopic,
          expectedTitle + " (Hörfassung)",
          expectedTime,
          expectedDuration,
          expectedDescription,
          expectedWebsite,
          new GeoLocations[]{expectedGeo},
          expectedUrlWithSubtitleSmall,
          expectedUrlWithSubtitleNormal,
          expectedUrlWithSubtitleHd,
          expectedSubtitle);

      film = iterator.next();
      AssertFilm.assertEquals(
          film,
          sender,
          expectedTopic,
          expectedTitle + " (Hörfilm)",
          expectedTime,
          expectedDuration,
          expectedDescription,
          expectedWebsite,
          new GeoLocations[]{expectedGeo},
          expectedUrlAudioDescSmall,
          expectedUrlAudioDescNormal,
          expectedUrlAudioDescHd,
          expectedSubtitle);
    }
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
