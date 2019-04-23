package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.funk.json.FunkVideoDeserializer;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestEndpoint;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestTask;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class FunkVideosRestTaskTest extends FunkTaskTestBase {

  @Test
  public void testVideoCount() {
    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_videos.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(3));
  }

  @Test
  public void testAllVideoInformation() {
    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_videos.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    final Set<FilmInfoDto> expected = new HashSet<>();

    final FilmInfoDto film1 = new FilmInfoDto("1605930");
    film1.setTopic("1045");
    film1.setTitle("Ansage an Hater, Reue, Reichtum uvm. | Money Boy im Talk + Live Performance");
    film1.setTime(parseTime("2019-04-21T15:00:00.000+0000"));
    film1.setDuration(Duration.ofSeconds(855));
    film1.setDescription(
        "Skrrt! Skrrt! Der Boy steppt ins Wohnzimmer und talkt über Hater, Reue, Reichtum und ein mögliches Feature mit Helene Fischer... Viel Fun! Yo!");
    film1.setWebsite(
        "https://www.funk.net/channel/world-wide-wohnzimmer-1045/ansage-an-hater-reue-reichtum-uvm-money-boy-im-talk-live-performance-1605930");
    expected.add(film1);

    final FilmInfoDto film2 = new FilmInfoDto("1600736");
    film2.setTopic("12011");
    film2.setTitle("PATCHWORK GANGSTA | Folge 02 Der Pakt mit dem Seytan");
    film2.setTime(parseTime("2019-03-17T07:42:01.000+0000"));
    film2.setDuration(Duration.ofSeconds(1662));
    film2.setDescription(
        "Franz ist sichtlich vom Bankalltag und vom Hunde-Hobby seiner Frau genervt. Auch die Launen seines Teenager-Sohnes Max tragen nicht zur Fröhlichkeit von Franz bei. Abends soll der Deal mit Amir stattfinden, doch haben Franz und Amir nicht mit der Einmischung durch Clan-Chef Yassin gerechnet.. Um Amir zu retten, gibt sich Franz als dessen Manager aus und ist nun verantwortlich für dessen Schulden.");
    film2.setWebsite(
        "https://www.funk.net/channel/patchwork-gangsta-12011/patchwork-gangsta-folge-02-der-pakt-mit-dem-seytan-1600736");
    expected.add(film2);

    final FilmInfoDto film3 = new FilmInfoDto("1600790");
    film3.setTopic("12011");
    film3.setTitle("PATCHWORK GANGSTA | Folge 03 Drogen-Drohnen-Deal");
    film3.setTime(parseTime("2019-03-17T23:43:49.000+0000"));
    film3.setDuration(Duration.ofSeconds(1558));
    film3.setDescription(
        "Um Yassin die 40.000 Euro zurück zu zahlen, beschließen Franz und Amir, den Novaya Zvezda Clan zu überfallen. Auf den wurden sie durch einen Tip von Amirs zwielichtigem Kumpel Axels aufmerksam. Der Coup gelingt, doch wie sollen die Drogen nun vercheckt werden? À la \"Dagobert\"... allerdings mit einem besseren Vehikel als einer Modelleisenbahn.");
    film3.setWebsite(
        "https://www.funk.net/channel/patchwork-gangsta-12011/patchwork-gangsta-folge-03-drogendrohnendeal-1600790");
    expected.add(film3);

    assertThat(actual, equalTo(expected));
  }

  @NotNull
  private LocalDateTime parseTime(final String dateTimeText) {
    return LocalDateTime.parse(
        dateTimeText, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
  }

  private Set<FilmInfoDto> executeTask(final String aRequestUrl) {
    final FunkCrawler crawler = createCrawler();
    return new FunkRestTask<>(
            crawler,
            new FunkRestEndpoint<>(
                FunkApiUrls.VIDEOS, new FunkVideoDeserializer(Optional.of(crawler))),
            createCrawlerUrlDto(aRequestUrl))
        .invoke();
  }
}
