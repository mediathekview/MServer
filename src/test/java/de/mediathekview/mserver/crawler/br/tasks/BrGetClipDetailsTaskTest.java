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
import de.mediathekview.mserver.crawler.br.BrClipQueryDto;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.testhelper.AssertFilm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class BrGetClipDetailsTaskTest extends BrTaskTestBase {

  static Stream<Arguments> data() {
    return Stream.of(
        arguments(
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
            GeoLocations.GEO_DE),
        arguments(
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
            GeoLocations.GEO_DE));
  }

  @ParameterizedTest
  @MethodSource("data")
  void test(
      final String id,
      final String filmJsonFile,
      final String expectedTopic,
      final String expectedTitle,
      final LocalDateTime expectedTime,
      final Duration expectedDuration,
      final String expectedDescription,
      final String expectedWebsite,
      final String expectedUrlSmall,
      final String expectedUrlNormal,
      final String expectedUrlHd,
      final String expectedSubtitle,
      final GeoLocations expectedGeo) {
    final Queue<BrClipQueryDto> queue = new ConcurrentLinkedQueue<>();
    queue.add(
        new BrClipQueryDto(
            wireMockServer.baseUrl() + "/graphql", new BrID(BrClipType.PROGRAMME, id)));

    setupSuccessfulJsonPostResponse("/graphql", filmJsonFile);

    final BrGetClipDetailsTask task = new BrGetClipDetailsTask(createCrawler(), queue);
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
