package de.mediathekview.mserver.crawler.ard.json;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.ArdConstants;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ForkJoinPool;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class ArdFilmDeserializerTest {

  private final String jsonFile;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedDateTime;
  private final Duration expectedDuration;
  private final String expectedUrlSmall;
  private final String expectedUrlNormal;
  private final String expectedUrlHd;
  private final String expectedSubtitle;
  private final GeoLocations expectedGeo;
  private final ArdFilmInfoDto[] relatedFilms;
  private final Optional<Sender> additionalSender;
  private final int expectedFilmCount;

  protected MServerConfigManager rootConfig =
      MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  public ArdFilmDeserializerTest(
      final String jsonFile,
      final String expectedTopic,
      final String expectedTitle,
      final String expectedDescription,
      final LocalDateTime expectedDateTime,
      final Duration expectedDuration,
      final String expectedUrlSmall,
      final String expectedUrlNormal,
      final String expectedUrlHd,
      final String expectedSubtitle,
      final GeoLocations expectedGeo,
      final ArdFilmInfoDto[] relatedFilms,
      final Optional<Sender> additionalSender) {
    this.jsonFile = jsonFile;
    this.expectedTopic = expectedTopic;
    this.expectedTitle = expectedTitle;
    this.expectedDescription = expectedDescription;
    this.expectedDateTime = expectedDateTime;
    this.expectedDuration = expectedDuration;
    this.expectedUrlSmall = expectedUrlSmall;
    this.expectedUrlNormal = expectedUrlNormal;
    this.expectedUrlHd = expectedUrlHd;
    this.expectedSubtitle = expectedSubtitle;
    this.expectedGeo = expectedGeo;
    this.relatedFilms = relatedFilms;
    this.additionalSender = additionalSender;
    expectedFilmCount = 1;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "/ard/ard_film_page11.json",
            "Tagesschau",
            "tagesschau, 09:00 Uhr",
            "Themen der Sendung: Bundestag und Bundesrat stimmen über Kohleausstieg ab, Werbeverbot für Tabak wird verschärft, Großbritannien lockert Corona-Einreisebeschränkungen, Zahl der Corona-Neuinfektionen in den USA erreicht neuen Höchststand, Urteil im Prozess gegen Menschenrechtler Steudtner in Istanbul erwartet, Hongkonger Bürgerrechtsaktivist bittet Deutschland um Hilfe für die Demokratie-Bewegung, \n.....",
            LocalDateTime.of(2020, 7, 3, 9, 0, 0),
            Duration.ofMinutes(4).plusSeconds(9),
            "https://media.tagesschau.de/video/2020/0703/TV-20200703-0912-2800.webml.h264.mp4",
            "https://media.tagesschau.de/video/2020/0703/TV-20200703-0912-2800.webl.h264.mp4",
            "https://media.tagesschau.de/video/2020/0703/TV-20200703-0912-2800.webxl.h264.mp4",
            "https://www.ardmediathek.de/subtitle/410890",
            GeoLocations.GEO_NONE,
            new ArdFilmInfoDto[0],
            Optional.empty()
          },
          {
            "/ard/ard_film_page_with_related11.json",
            "Live nach neun",
            "Live nach Neun",
            "",
            LocalDateTime.of(2020, 7, 3, 9, 5, 0),
            Duration.ofMinutes(49).plusSeconds(46),
            "https://pdvideosdaserste-a.akamaihd.net/de/2020/07/03/live_20200703_070454_sendeton_640x360-25p-1300kbit.mp4",
            "https://pdvideosdaserste-a.akamaihd.net/de/2020/07/03/live_20200703_070454_sendeton_960x540-50p-2600kbit.mp4",
            "https://pdvideosdaserste-a.akamaihd.net/de/2020/07/03/live_20200703_070454_sendeton_1920x1080-50p-8000kbit.mp4",
            "https://www.ardmediathek.de/subtitle/410911",
            GeoLocations.GEO_NONE,
            new ArdFilmInfoDto[] {
              new ArdFilmInfoDto(
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuL2U2NjBiMDU0LWM3YmYtNDdkYy1iMmFlLWM1N2NkNmM1MjVhZA",
                  ArdConstants.ITEM_URL
                      + "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuL2U2NjBiMDU0LWM3YmYtNDdkYy1iMmFlLWM1N2NkNmM1MjVhZA",
                  0),
              new ArdFilmInfoDto(
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzc2NjcyOTI0LWNmNjMtNDhkNy05ZTcwLTQ1Y2EzYmZmZTUzMg",
                  ArdConstants.ITEM_URL
                      + "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzc2NjcyOTI0LWNmNjMtNDhkNy05ZTcwLTQ1Y2EzYmZmZTUzMg",
                  0),
              new ArdFilmInfoDto(
                  "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzhmZWRlOTE2LTg4NmMtNDZhNy1iNmI5LTQ5NmMzMWJlNWZiZQ",
                  ArdConstants.ITEM_URL
                      + "Y3JpZDovL2Rhc2Vyc3RlLmRlL2xpdmUgbmFjaCBuZXVuLzhmZWRlOTE2LTg4NmMtNDZhNy1iNmI5LTQ5NmMzMWJlNWZiZQ",
                  0)
            },
            Optional.empty()
          },
          {
            "/ard/ard_film_page_ndr11.json",
            "Wer weiß denn sowas? | 03.07.2020",
            "Wer weiß denn sowas? | 03.07.2020",
            "Das beliebte Wissensspiel mit Bernhard Hoëcker und Elton. Moderator Kai Pflaume präsentiert unglaubliche Rätselfragen. Welches Team gewinnt? Gäste: Hardy Krüger jr. und Oliver Masucci.",
            LocalDateTime.of(2020, 7, 3, 16, 25, 0),
            Duration.ofMinutes(44).plusSeconds(14),
            "https://mediandr-a.akamaihd.net/progressive/2020/0703/TV-20200703-1726-5500.ln.mp4",
            "https://mediandr-a.akamaihd.net/progressive/2020/0703/TV-20200703-1726-5500.hq.mp4",
            "https://mediandr-a.akamaihd.net/progressive/2020/0703/TV-20200703-1726-5500.hd.mp4",
            "https://www.ardmediathek.de/subtitle/411033",
            GeoLocations.GEO_NONE,
            new ArdFilmInfoDto[0],
            Optional.of(Sender.NDR)
          }
        });
  }

  @Test
  public void test() {

    final JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    final ArdFilmDeserializer target = new ArdFilmDeserializer(createCrawler());
    final List<ArdFilmDto> actualFilms = target.deserialize(jsonElement, null, null);

    assertThat(actualFilms.size(), equalTo(expectedFilmCount));
    final ArdFilmDto[] films = actualFilms.toArray(new ArdFilmDto[] {});
    AssertFilm.assertEquals(
        films[0].getFilm(),
        additionalSender.orElse(Sender.ARD),
        expectedTopic,
        expectedTitle,
        expectedDateTime,
        expectedDuration,
        expectedDescription,
        "",
        new GeoLocations[] {expectedGeo},
        expectedUrlSmall,
        expectedUrlNormal,
        expectedUrlHd,
        expectedSubtitle);
    assertThat(films[0].getRelatedFilms(), Matchers.containsInAnyOrder(relatedFilms));
  }

  protected ArdCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
