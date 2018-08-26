package de.mediathekview.mserver.crawler.rbb.parser;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.rbb.RbbConstants;
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
public class RbbFilmDetailDeserializerTest {

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {
                "http://mediathek.rbb-online.de/tv/Film-im-rbb/Zwei-Millionen-suchen-einen-Vater/rbb-Fernsehen/Video?bcastId=10009780&documentId=50543576",
                "/rbb/rbb_film_with_subtitle.html",
                "https://mediathek.rbb-online.de/play/media/50543576?devicetype=pc&features=hls",
                "Film im rbb",
                "Zwei Millionen suchen einen Vater",
                "Eine gewitzte Hotelinhaberin tut alles, um das Sorgerecht für ihren Schützling zu bekommen. ",
                LocalDateTime.of(2018, 3, 3, 14, 25, 0),
                Duration.ofHours(1).plusMinutes(28).plusSeconds(41),}
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

  public RbbFilmDetailDeserializerTest(final String aRequestUrl, final String aHtmlPage, final String aExpectedVideoUrl,
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

    final RbbFilmDetailDeserializer target = new RbbFilmDetailDeserializer(RbbConstants.URL_BASE);
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
