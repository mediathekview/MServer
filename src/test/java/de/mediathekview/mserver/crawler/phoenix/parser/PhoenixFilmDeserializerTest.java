package de.mediathekview.mserver.crawler.phoenix.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.testhelper.FileReader;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class PhoenixFilmDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "https://www.phoenix.de/content/2560924",
                "/phoenix/phoenix_film_detail1.html",
                "hhttps://tmd.phoenix.de/tmd/2/ngplayer_2_3/vod/ptmd/phoenix/180325_phx_presseclub",
                "Presseclub",
                "Zwischen Islamdebatte und Heimatministerium – wer gehört zu Deutschland?",
                "Zu Gast sind Wolfgang Bok, Yassin Musharbash, Elisabeth Niejahr und Dagmar Rosenfeld.",
                LocalDateTime.of(2018, 3, 25, 12, 00, 0),
                Duration.ofMinutes(56).plusSeconds(40)
            }
        });
  }

  private final String requestUrl;
  private final String htmlPage;
  private final String expectedVideoUrl;
  private final String expectedTopic;
  private final String expectedTitle;
  private final String expectedDescription;
  private final LocalDateTime expectedTime;
  private final Duration expectedDuration;

  public PhoenixFilmDeserializerTest(final String aRequestUrl, final String aHtmlPage, final String aExpectedVideoUrl,
      final String aExpectedTopic, final String aExpectedTitle, final String aExpectedDescription, final LocalDateTime aExpectedTime,
      final Duration aExpectedDuration) {

    requestUrl = aRequestUrl;
    htmlPage = aHtmlPage;
    expectedVideoUrl = aExpectedVideoUrl;
    expectedTopic = aExpectedTopic;
    expectedTitle = aExpectedTitle;
    expectedDescription = aExpectedDescription;
    expectedTime = aExpectedTime;
    expectedDuration = aExpectedDuration;
  }

  @Test
  public void deserializeTest() {

    final String htmlContent = FileReader.readFile(htmlPage);
    final Document document = Jsoup.parse(htmlContent);

    final PhoenixFilmDeserializer target = new PhoenixFilmDeserializer();
    final Optional<FilmInfoDto> actual = target.deserialize(new CrawlerUrlDTO(requestUrl), document);

    assertThat(actual.isPresent(), equalTo(true));
    FilmInfoDto dto = actual.get();

    assertThat(dto.getTopic(), equalTo(expectedTopic));
    assertThat(dto.getTitle(), equalTo(expectedTitle));
    assertThat(dto.getDescription(), equalTo(expectedDescription));
    assertThat(dto.getWebsite(), equalTo(requestUrl));
    assertThat(dto.getDuration(), equalTo(expectedDuration));
    assertThat(dto.getTime(), equalTo(expectedTime));
    assertThat(dto.getUrl(), equalTo(expectedVideoUrl));
  }
}
