package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

@ExtendWith(MockitoExtension.class)
public class SrFilmDetailTaskNoFilmTest extends SrTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {
            "https://www.sr-mediathek.de/index.php?seite=7&id=15808&pnr=0",
            "/sr/sr_podcast_page.html",
            "Abendrot",
            null,
            null
          },
          {
            "https://www.sr-mediathek.de/index.php?seite=7&id=57773",
            "/sr/sr_audio_page.html",
            "Bücherlese",
            null,
            null
          },
          {
            "https://www.sr-mediathek.de/index.php?seite=7&id=39741",
            "/sr/sr_film_page3_fsk.html",
            "Tatort",
            "/sr_player/mc.php?id=39741&tbl=&pnr=0&hd=0&devicetype=",
            "/sr/sr_film_video_details3_fsk.json"
          }
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void test(final String requestUrl, final String filmPageFile, final String theme, final String videoDetailsUrl, final String videoDetailsFile) {
    jsoupConnection = JsoupMock.mock(requestUrl, filmPageFile);
    SrCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    if (videoDetailsUrl != null) {
      setupSuccessfulJsonResponse(videoDetailsUrl, videoDetailsFile);
    }

    final Set<Film> actual = executeTask(crawler, theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<Film> executeTask(final SrCrawler crawler, final String aTheme, final String aRequestUrl) {
    return new SrFilmDetailTask(
        crawler, createCrawlerUrlDto(aTheme, aRequestUrl), getWireMockBaseUrlSafe())
        .invoke();
  }
}
