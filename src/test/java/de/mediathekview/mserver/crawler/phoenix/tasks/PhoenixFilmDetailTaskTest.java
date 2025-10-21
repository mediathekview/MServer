package de.mediathekview.mserver.crawler.phoenix.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.GeoLocations;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
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
  private final String filmDetailUrl;
  private final String filmDetailFile;
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
  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  public PhoenixFilmDetailTaskTest(
      final String aFilmUrl,
      final String aFilmJsonFile,
      final String aFilmDetailUrl,
      final String aFilmDetailFile,
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
    filmDetailUrl = aFilmDetailUrl;
    filmDetailFile = aFilmDetailFile;
    videoUrl = aVideoUrl;
    videoJsonFile = aVideoJsonFile;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
    expectedDescription = aExpectedDescription;
    expectedWebsite = aExpectedWebsite;
    expectedUrlSmall = buildWireMockUrl(aExpectedUrlSmall);
    expectedUrlNormal = buildWireMockUrl(aExpectedUrlNormal);
    expectedUrlHd = buildWireMockUrl(aExpectedUrlHd);
    expectedSubtitle = buildWireMockUrl(aExpectedSubtitle);
    expectedGeo = aExpectedGeo;
  }

  @Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/response/id/2121010",
            "/phoenix/phoenix_film_detail2_reponse.json",
            "/php/mediaplayer/data/beitrags_details.php?id=2121010",
            "/phoenix/phoenix_film_detail2_beitrag_details.json",
            "/tmd/2/android_native_5/vod/ptmd/phoenix/210416_phx_doku_awri_2",
            "/phoenix/phoenix_film_detail2_video.json",
            "Alles was Recht ist",
            "(2/5): Persönlichkeit oder Öffentlichkeit?",
            LocalDateTime.of(2021, 4, 16, 15, 40, 0),
            Duration.ofMinutes(15).plusSeconds(1),
            "Dürfen Medien die Namen Prominenter nennen, wenn gegen sie ermittelt wird? Diese Frage stellte sich zuletzt im Fall von Ex-Fußballnationalspieler Christoph Metzelder. Was ist wichtiger? Persönlichkeitsrechte oder das Interesse der Öffentlichkeit? Richterinnen und Richter müssen das von Fall zu Fall aufs Neue entscheiden und stecken dabei in einem juristischen Dilemma: Der Schutz der Persönlichkeit\n.....",
            "https://www.phoenix.de/sendungen/dokumentationen/alles-was-recht-ist/25-persoenlichkeit-oder-effentlichkeit-a-2120685.html",
            "/none/phoenix/21/04/210416_phx_doku_awri_2/2/210416_phx_doku_awri_2_776k_p11v13.mp4",
            "/none/phoenix/21/04/210416_phx_doku_awri_2/2/210416_phx_doku_awri_2_2328k_p35v13.mp4",
            "",
            "",
            GeoLocations.GEO_NONE
          },
          {
            "/response/id/2320549",
            "/phoenix/phoenix_film_detail3_response.json",
            "/php/mediaplayer/data/beitrags_details.php?id=2361354",
            "/phoenix/phoenix_film_detail3_beitrag_details.json",
            "/tmd/2/android_native_5/vod/ptmd/phoenix/211114_1200_phx_presseclub",
            "/phoenix/phoenix_film_detail3_video.json",
            "Presseclub",
            "Handeln statt Reden: Impfpflicht für alle?",
            LocalDateTime.of(2021, 11, 14, 12, 0, 0),
            Duration.ofMinutes(58).plusSeconds(56),
            "Corona ist mit voller Wucht zurück: Noch nie seit Beginn der Pandemie haben sich so viele Menschen infiziert wie in dieser Woche. Das RKI meldet täglich neue Rekordwerte bei der 7-Tage-Inzidenz. In Altenheimen sterben wieder Menschen und in manchen Regionen wie in Sachsen, Thüringen oder Bayern werden Intensivbetten knapp. Das kann tödlich sein - nicht nur für Covid-Kranke, sondern auch für Unfall\n.....",
            "https://www.phoenix.de/sendungen/gespraeche/presseclub/handeln-statt-reden-impfpflicht-fuer-alle-a-2320549.html",
            "/de/phoenix/21/11/211114_1200_phx_presseclub/1/211114_1200_phx_presseclub_776k_p11v13.mp4",
            "/de/phoenix/21/11/211114_1200_phx_presseclub/1/211114_1200_phx_presseclub_1496k_p13v13.mp4",
            "",
            "",
            GeoLocations.GEO_DE
          }
        });
  }

  @Test
  public void test() {
    setupSuccessfulJsonResponse(filmUrl, filmJsonFile);
    setupSuccessfulJsonResponse(filmDetailUrl, filmDetailFile);
    setupSuccessfulJsonResponse(videoUrl, videoJsonFile);
    setupHeadResponse(404);
    setupHeadResponse(
        "/none/phoenix/21/04/210416_phx_doku_awri_2/2/210416_phx_doku_awri_2_2328k_p35v13.mp4",
        200);

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
    urls.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + aDetailUrl));
    return new PhoenixFilmDetailTask(createCrawler(), urls, null, getWireMockBaseUrlSafe())
        .invoke();
  }
}
