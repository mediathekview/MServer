package de.mediathekview.mserver.crawler.arte.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.arte.ArteCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ArteFilmDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/arte/arte_film_detail1.json",
                Sender.ARTE_DE,
                "Kultur und Pop - Kunst",
                "Stadt Land Kunst Spezial 2019 (2/26) - Bali",
                "Diese Woche mit Fokus Bali: (1) Eat Pray Love. Erloschene Vulkane und grüne Reisterrassen so weit das Auge reicht: Bali ist der ideale Rahmen für eine Wiedergeburt! (2) Balis besonderes Korn: Das balinesische Herz schlägt in den Reisfeldern im Zentrum der Insel. (3) Das absolute Muss: Charlie Chaplins Indonesien voller Reisterrassen, Vulkane und buddhistischer Tempel.",
                LocalDateTime.of(2019, 1, 12, 16, 35, 0),
                Duration.ofMinutes(37).plusSeconds(51),
                GeoLocations.GEO_DE_AT_CH_EU
            }
        });
  }

  private final String jsonFile;
  private final Sender sender;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedDateTime;
  private final Duration expectedDuration;
  private final GeoLocations[] expectedGeo;

  public ArteFilmDeserializerTest(final String jsonFile,
      final Sender sender,
      final String expectedTopic, final String expectedTitle,
      final String expectedDescription,
      final LocalDateTime expectedDateTime, final Duration expectedDuration,
      final GeoLocations expectedGeo) {
    this.jsonFile = jsonFile;
    this.sender = sender;
    this.expectedTopic = expectedTopic;
    this.expectedTitle = expectedTitle;
    this.expectedDescription = expectedDescription;
    this.expectedDateTime = expectedDateTime;
    this.expectedDuration = expectedDuration;
    this.expectedGeo = new GeoLocations[]{expectedGeo};
  }

  @Test
  public void test() {

    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    ArteFilmDeserializer target = new ArteFilmDeserializer(sender, LocalDateTime.of(2019, 1, 11, 16, 0, 0));
    Optional<Film> film = target.deserialize(jsonElement, null, null);

    assertThat(film.isPresent(), equalTo(true));
    Film actualFilm = film.get();
    assertThat(actualFilm.getSender(), equalTo(sender));
    assertThat(actualFilm.getThema(), equalTo(expectedTopic));
    assertThat(actualFilm.getTitel(), equalTo(expectedTitle));
    assertThat(actualFilm.getBeschreibung(), equalTo(expectedDescription));
    assertThat(actualFilm.getTime(), equalTo(expectedDateTime));
    assertThat(actualFilm.getDuration(), equalTo(expectedDuration));
    assertThat(actualFilm.getGeoLocations(), Matchers.containsInAnyOrder(expectedGeo));
  }

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected ArteCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArteCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

}
