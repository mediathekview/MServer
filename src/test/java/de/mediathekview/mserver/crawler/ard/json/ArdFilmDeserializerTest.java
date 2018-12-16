package de.mediathekview.mserver.crawler.ard.json;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.gson.JsonElement;
import de.mediathekview.mlib.daten.GeoLocations;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import de.mediathekview.mserver.crawler.ard.ArdFilmDto;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.AssertFilm;
import de.mediathekview.mserver.testhelper.JsonFileReader;
import java.net.URLEncoder;
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
public class ArdFilmDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "/ard/ard_film_page1.json",
                "Sturm der Liebe",
                "Die schönsten Momente: Eva und Robert",
                "Dieses Special widmet sich der Liebesgeschichte von Eva und Robert. Es beleuchtet Roberts Trauerphase, aber auch die Rückkehr von Evas tot geglaubter erster großen Liebe Markus.",
                LocalDateTime.of(2018, 12, 5, 15, 10, 0),
                Duration.ofMinutes(47).plusSeconds(36),
                "https://pdvideosdaserste-a.akamaihd.net/int/2018/12/05/c0c43211-2627-4a68-8757-be43c0dad75a/512-1.mp4",
                "https://pdvideosdaserste-a.akamaihd.net/int/2018/12/05/c0c43211-2627-4a68-8757-be43c0dad75a/960-1.mp4",
                "https://pdvideosdaserste-a.akamaihd.net/int/2018/12/05/c0c43211-2627-4a68-8757-be43c0dad75a/1280-1.mp4",
                "",
                GeoLocations.GEO_NONE,
                new ArdFilmInfoDto[] {
                    new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS8yNGY1ZTU4My01YTBhLTRmNzItOThhZi1lNzBiYjU1NGY5MDA",
                        "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS8yNGY1ZTU4My01YTBhLTRmNzItOThhZi1lNzBiYjU1NGY5MDA\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
                        0),
                    new ArdFilmInfoDto("Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS81Y2NiMjFmZS1kZDk3LTRlZDYtYjRhZS05ZjNjOWQ5ZjQ2MjE",
                        "https://api.ardmediathek.de/public-gateway?variables=" + URLEncoder.encode("{\"client\":\"ard\",\"clipId\":\"Y3JpZDovL2Rhc2Vyc3RlLmRlL3N0dXJtIGRlciBsaWViZS81Y2NiMjFmZS1kZDk3LTRlZDYtYjRhZS05ZjNjOWQ5ZjQ2MjE\",\"deviceType\":\"pc\"}") + "&extensions=" + URLEncoder.encode("{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"a9a9b15083dd3bf249264a7ff5d9e1010ec5d861539bc779bb1677a4a37872da\"}}"),
                        0)
                }
            },
            {
                "/ard/ard_film_page2.json",
                "BRISANT",
                "Brisant vom 06.12.2018",
                "+++ Mutmaßliches Beziehungsdrama: Elfjähriger findet im Sterben liegende Mutter +++ Warnung: Betrüger geben sich als Polizisten aus +++ Was machen deutsche Promis am Nikolaustag? +++ (Nur in D abrufbar)",
                LocalDateTime.of(2018, 12, 6, 17, 15, 0),
                Duration.ofMinutes(32).plusSeconds(33),
                "https://odgeomdr-a.akamaihd.net/mp4dyn2/c/FCMS-c725e5f0-78c4-4f26-8e05-0848d12c2f50-9a4bb04739be_c7.mp4",
                "https://odgeomdr-a.akamaihd.net/mp4dyn2/c/FCMS-c725e5f0-78c4-4f26-8e05-0848d12c2f50-730aae549c28_c7.mp4",
                "https://odgeomdr-a.akamaihd.net/mp4dyn2/c/FCMS-c725e5f0-78c4-4f26-8e05-0848d12c2f50-be7c2950aac6_c7.mp4",
                "https://www.ardmediathek.de/subtitle/271940",
                GeoLocations.GEO_DE,
                new ArdFilmInfoDto[] {}
            },
            {
                "/ard/ard_film_page3.json",
                "Sportschau",
                "Jens Voigt über Sinn und Unsinn von Radcomputern",
                "Jens Voigt erklärt, warum moderne Bordcomputer gut sind, man sich im Rennen aber trotzdem nicht nur auf die Daten verlassen sollte.",
                LocalDateTime.of(2018, 6, 30, 10, 0, 0),
                Duration.ofSeconds(113),
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/167/1678446/1678446_19454306.mp4",
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/167/1678446/1678446_19454308.mp4",
                "http://wdrmedien-a.akamaihd.net/medp/ondemand/weltweit/fsk0/167/1678446/1678446_19454307.mp4",
                "",
                GeoLocations.GEO_NONE,
                new ArdFilmInfoDto[] {}
            },
            {
                "/ard/ard_film_page4.json",
                "Brennpunkt",
                "Brennpunkt: Deutschland hat gewählt",
                "Die Wählerinnen und Wähler haben die Bundeskanzlerin Angela Merkel im Amt bestätigt und ihr den Auftrag zur Regierungsbildung erteilt. Welche Koalitionsoptionen stehen der CDU-Vorsitzenden für die kommenden vier Jahre zur Verfügung?",
                LocalDateTime.of(2013, 9, 23, 20, 15, 0),
                Duration.ofSeconds(2845),
                "https://media.tagesschau.de/video/2013/0923/TV-20130923-2151-4101.webm.h264.mp4",
                "https://media.tagesschau.de/video/2013/0923/TV-20130923-2151-4101.webl.h264.mp4",
                "",
                "https://classic.ardmediathek.de/static/avportal/untertitel_mediathek_preview/17256458.xml",
                GeoLocations.GEO_NONE,
                new ArdFilmInfoDto[] {}
            }
        });
  }

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

  public ArdFilmDeserializerTest(final String jsonFile,
      final String expectedTopic, final String expectedTitle,
      final String expectedDescription,
      final LocalDateTime expectedDateTime, final Duration expectedDuration,
      final String expectedUrlSmall, final String expectedUrlNormal,
      final String expectedUrlHd, final String expectedSubtitle,
      final GeoLocations expectedGeo, final ArdFilmInfoDto[] relatedFilms) {
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
  }

  @Test
  public void test() {

    JsonElement jsonElement = JsonFileReader.readJson(jsonFile);

    ArdFilmDeserializer target = new ArdFilmDeserializer(createCrawler());
    Optional<ArdFilmDto> film = target.deserialize(jsonElement, null, null);

    assertThat(film.isPresent(), equalTo(true));
    AssertFilm
        .assertEquals(film.get().getFilm(), Sender.ARD, expectedTopic, expectedTitle, expectedDateTime, expectedDuration,
            expectedDescription,
            "",
            new GeoLocations[]{expectedGeo}, expectedUrlSmall, expectedUrlNormal, expectedUrlHd, expectedSubtitle);
    assertThat(film.get().getRelatedFilms(), Matchers.containsInAnyOrder(relatedFilms));
  }

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected ArdCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArdCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

}
