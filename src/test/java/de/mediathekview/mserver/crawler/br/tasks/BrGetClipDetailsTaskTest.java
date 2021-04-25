/*
 * BrGetClipDetailsTaskTest.java
 *
 * Projekt    : MServer
 * erstellt am: 25.12.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.crawler.br.tasks;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.br.BrClipQueryDto;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class BrGetClipDetailsTaskTest extends WireMockTestBase {

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
  private final String id;

  public BrGetClipDetailsTaskTest(
      final String id,
      final String aFilmJsonFile,
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
    this.id = id;
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

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "av:6040d1a28997e2001339b3be",
            "/br/br_film_with_subtitle.json",
            "Planet Wissen",
            "Wikipedia – Was bringt die freie Enzyklopädie?",
            LocalDateTime.of(2021, 4, 13, 18, 15, 0),
            Duration.ofMinutes(58).plusSeconds(11),
            "Am 15. Januar 2001 wurde Wikipedia gegründet. Als „gemeinnütziges Projekt zur Erstellung einer freien Internet-Enzyklopädie in zahlreichen Sprachen“ - so beschreibt sich Wikipedia selbst. Was klein anfing, ist heute ein Massenmedium: Wikipedia zählt zu den weltweit meistbesuchten Internetseiten, mehr als 55 Millionen Artikel sind online, übersetzt in mehrere hundert Sprachen. Frei zugängliches Wis\n.....",
            "https://www.br.de/mediathek/video/planet-wissen-wissensmagazin-wikipedia-was-bringt-die-freie-enzyklopaedie-av:6040d1a28997e2001339b3be",
            "https://cdn-storage.br.de/geo/b7/2021-04/13/fa4cb02e9c7b11eb869202420a000515_E.mp4",
            "https://cdn-storage.br.de/geo/b7/2021-04/13/fa4cb02e9c7b11eb869202420a000515_C.mp4",
            "https://cdn-storage.br.de/geo/b7/2021-04/13/fa4cb02e9c7b11eb869202420a000515_X.mp4",
            "https://www.br.de/untertitel/a3e8c27f-f15b-4cc7-881a-bf021b8aac5c.ttml",
            GeoLocations.GEO_DE,
          },
          {
            "av:5be427420d112e0018031587",
            "/br/br_film_with_geo.json",
            "Gernstl unterwegs",
            "Gernstl in Oberfranken (1/2)",
            LocalDateTime.of(2021, 5, 9, 15, 0, 0),
            Duration.ofMinutes(13).plusSeconds(37),
            "Das filmische Pfadfinderteam ist wieder unterwegs: Franz X. Gernstl, HP Fischer (Kamera) und Stefan Ravasz (Ton) haben sich die sieben Regierungsbezirke vorgenommen. Sie wollen herauszufinden, wie die Bayern sind. Was sie ausmacht, die Franken, die Schwaben und die Altbayern. Was sie gemeinsam haben, und was sie unterscheidet.",
            "https://www.br.de/mediathek/video/gernstl-sieben-mal-bayern-reportage-gernstl-in-oberfranken-1-2-av:5be427420d112e0018031587",
            "https://cdn-storage.br.de/geo/b7/2018-11/13/5fed645ae75e11e8bca2984be109059a_E.mp4",
            "https://cdn-storage.br.de/geo/b7/2018-11/13/5fed645ae75e11e8bca2984be109059a_C.mp4",
            "https://cdn-storage.br.de/geo/b7/2018-11/13/5fed645ae75e11e8bca2984be109059a_X.mp4",
            "",
            GeoLocations.GEO_DE,
          }
        });
  }

  protected MServerConfigManager rootConfig =
      MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected BrCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new BrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  @Test
  public void test() {
    final Queue<BrClipQueryDto> queue = new ConcurrentLinkedQueue<>();
    queue.add(new BrClipQueryDto(wireMockServer.baseUrl() + "/graphql", new BrID(BrClipType.PROGRAMME, id)));

    setupSuccessfulJsonPostResponse("/graphql", filmJsonFile);

    final BrGetClipDetailsTask task =
        new BrGetClipDetailsTask(createCrawler(), queue);
    final Set<Film> actual = task.invoke();
    assertThat(actual.size(), equalTo(1));

    final Film film = actual.iterator().next();
    AssertFilm.assertEquals(
        film,
        Sender.BR,
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
}
