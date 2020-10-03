package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class SrTopicArchivePageTaskTest extends SrTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void testOverviewWithSinglePage() throws IOException {
    final String theme = "2 Mann für alle Gänge";

    final SrTopicUrlDTO[] expectedUrls =
        new SrTopicUrlDTO[] {
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49674"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49442"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49171"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=48954")
        };

    final String requestUrl = "srf_sample.html";
    final Connection connection =
        JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_single.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final Set<SrTopicUrlDTO> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testOverviewWithMultiplePages() throws IOException {
    final String theme = "Meine Traumreise";
    final SrTopicUrlDTO[] expectedUrls =
        new SrTopicUrlDTO[] {
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=54623"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=54536"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=54310"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=54078"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=53895"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=52595"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=52317"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=51814"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=51668"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=33014"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=51200"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=44118"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49170"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=48941"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=48761"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=48574"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=38815"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=47765"),
          new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=47554")
        };

    final String requestUrl = "https://www.sr-mediathek.de/index.php?seite=10&sen=MT&s=1";

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(requestUrl, "/sr/sr_sendung_overview_page1.html");
    urlMapping.put(
        "https://www.sr-mediathek.de/index.php?seite=10&sen=MT&s=2",
        "/sr/sr_sendung_overview_page2.html");
    final Map<String, Connection> connections = JsoupMock.mock(urlMapping);
    connections.forEach(
        (url, currentConnection) -> {
          try {
            when(jsoupConnection.getConnection(eq(url))).thenReturn(currentConnection);
          } catch (final IOException iox) {
            fail();
          }
        });

    final Set<SrTopicUrlDTO> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testOverviewEmpty() throws IOException {
    final SrTopicUrlDTO[] expectedUrls = new SrTopicUrlDTO[0];

    final String requestUrl = "srf_sample.html";
    final Connection connection =
        JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_empty.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final Set<SrTopicUrlDTO> actual = executeTask("Test", requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testOverviewAudioFiles() throws IOException {
    final SrTopicUrlDTO[] expectedUrls = new SrTopicUrlDTO[0];

    final String requestUrl = "srf_sample.html";
    final Connection connection =
        JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_audio.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final Set<SrTopicUrlDTO> actual = executeTask("Test", requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  private Set<SrTopicUrlDTO> executeTask(final String aTheme, final String aRequestUrl) {
    return new SrTopicArchivePageTask(
            createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl), jsoupConnection)
        .invoke();
  }
}
