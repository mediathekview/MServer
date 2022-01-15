package de.mediathekview.mserver.crawler.funk;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.basic.PagedElementListDTO;
import de.mediathekview.mserver.crawler.funk.json.FunkVideoDeserializer;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;

import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(Parameterized.class)
public class FunkVideoDeserializerTest {

  private final String jsonFile;
  private final PagedElementListDTO<FilmInfoDto> correctResults;

  public FunkVideoDeserializerTest(
      final String jsonFile, final PagedElementListDTO<FilmInfoDto> correctResults) {
    this.jsonFile = jsonFile;
    this.correctResults = correctResults;
  }

  @Parameterized.Parameters
  public static Object[][] data() {
    final PagedElementListDTO<FilmInfoDto> videos = new PagedElementListDTO<>();
    videos.setNextPage(Optional.empty());
    final FilmInfoDto film1 = new FilmInfoDto("https://api.nexx.cloud/v3/741/videos/byid/1605930");
    film1.setTopic("1045");
    film1.setTitle("Ansage an Hater, Reue, Reichtum uvm. | Money Boy im Talk + Live Performance");
    film1.setTime(parseTime("2019-04-21T15:00:00.000+0000"));
    film1.setDuration(Duration.ofSeconds(855));
    film1.setDescription(
        "Skrrt! Skrrt! Der Boy steppt ins Wohnzimmer und talkt über Hater, Reue, Reichtum und ein mögliches Feature mit Helene Fischer... Viel Fun! Yo!");
    film1.setWebsite(
        "https://www.funk.net/channel/world-wide-wohnzimmer-1045/ansage-an-hater-reue-reichtum-uvm-money-boy-im-talk-live-performance-1605930");
    videos.addElement(film1);

    final FilmInfoDto film2 = new FilmInfoDto("https://api.nexx.cloud/v3/741/videos/byid/1600736");
    film2.setTopic("12011");
    film2.setTitle("PATCHWORK GANGSTA | Folge 02 Der Pakt mit dem Seytan");
    film2.setTime(parseTime("2019-03-17T07:42:01.000+0000"));
    film2.setDuration(Duration.ofSeconds(1662));
    film2.setDescription(
        "Franz ist sichtlich vom Bankalltag und vom Hunde-Hobby seiner Frau genervt. Auch die Launen seines Teenager-Sohnes Max tragen nicht zur Fröhlichkeit von Franz bei. Abends soll der Deal mit Amir stattfinden, doch haben Franz und Amir nicht mit der Einmischung durch Clan-Chef Yassin gerechnet.. Um Amir zu retten, gibt sich Franz als dessen Manager aus und ist nun verantwortlich für dessen Schulden.");
    film2.setWebsite(
        "https://www.funk.net/channel/patchwork-gangsta-12011/patchwork-gangsta-folge-02-der-pakt-mit-dem-seytan-1600736");
    videos.addElement(film2);

    final FilmInfoDto film3 = new FilmInfoDto("https://api.nexx.cloud/v3/741/videos/byid/1600790");
    film3.setTopic("12011");
    film3.setTitle("PATCHWORK GANGSTA | Folge 03 Drogen-Drohnen-Deal");
    film3.setTime(parseTime("2019-03-17T23:43:49.000+0000"));
    film3.setDuration(Duration.ofSeconds(1558));
    film3.setDescription(
        "Um Yassin die 40.000 Euro zurück zu zahlen, beschließen Franz und Amir, den Novaya Zvezda Clan zu überfallen. Auf den wurden sie durch einen Tip von Amirs zwielichtigem Kumpel Axels aufmerksam. Der Coup gelingt, doch wie sollen die Drogen nun vercheckt werden? À la \"Dagobert\"... allerdings mit einem besseren Vehikel als einer Modelleisenbahn.");
    film3.setWebsite(
        "https://www.funk.net/channel/patchwork-gangsta-12011/patchwork-gangsta-folge-03-drogendrohnendeal-1600790");
    videos.addElement(film3);
    return new Object[][] {{"/funk/funk_video_page_last.json", videos}};
  }

  @NotNull
  private static LocalDateTime parseTime(final String dateTimeText) {
    return LocalDateTime.parse(
        dateTimeText, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ"));
  }

  @Test
  public void testDeserialize() throws URISyntaxException, IOException {
    final Type funkVideosType = new TypeToken<PagedElementListDTO<FilmInfoDto>>() {}.getType();
    final MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    final MServerBasicConfigDTO senderConfig = rootConfig.getSenderConfig(Sender.FUNK);
    senderConfig.setMaximumSubpages(2);

    final FunkCrawler mockedFunkCrawler = Mockito.mock(FunkCrawler.class);
    Mockito.when(mockedFunkCrawler.getRuntimeConfig()).thenReturn(rootConfig.getConfig());
    Mockito.when(mockedFunkCrawler.incrementMaxCountBySizeAndGetNewSize(Mockito.anyLong()))
        .thenReturn(1L);

    final Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(
                funkVideosType, new FunkVideoDeserializer(mockedFunkCrawler))
            .create();

    final PagedElementListDTO<FilmInfoDto> videoResultList =
        gson.fromJson(
            Files.newBufferedReader(Paths.get(getClass().getResource(jsonFile).toURI())),
            funkVideosType);

    assertThat(videoResultList, equalTo(correctResults));
  }
}
