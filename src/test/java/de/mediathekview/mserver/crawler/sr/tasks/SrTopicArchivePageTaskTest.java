package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
        value = {
                "javax.net.ssl.*",
                "javax.*",
                "com.sun.*",
                "org.apache.logging.log4j.core.config.xml.*"
        })
public class SrTopicArchivePageTaskTest extends SrTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() throws IOException {
    String theme = "2 Mann für alle Gänge";

      SrTopicUrlDTO[] expectedUrls =
              new SrTopicUrlDTO[]{
                      new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49674"),
                      new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49442"),
                      new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=49171"),
                      new SrTopicUrlDTO(theme, "https://www.sr-mediathek.de/index.php?seite=7&id=48954")
              };

    String requestUrl = "srf_sample.html";
    JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_single.html");

    final Set<SrTopicUrlDTO> actual = executeTask(theme, requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testOverviewWithMultiplePages() throws IOException {
    String theme = "Meine Traumreise";
      SrTopicUrlDTO[] expectedUrls =
              new SrTopicUrlDTO[]{
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

    String requestUrl = "https://www.sr-mediathek.de/index.php?seite=10&sen=MT&s=1";

    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(requestUrl, "/sr/sr_sendung_overview_page1.html");
      urlMapping.put(
              "https://www.sr-mediathek.de/index.php?seite=10&sen=MT&s=2",
              "/sr/sr_sendung_overview_page2.html");
    JsoupMock.mock(urlMapping);

      final Set<SrTopicUrlDTO> actual = executeTask(theme, requestUrl);

      assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testOverviewEmpty() throws IOException {
    SrTopicUrlDTO[] expectedUrls = new SrTopicUrlDTO[0];

    String requestUrl = "srf_sample.html";
    JsoupMock.mock(requestUrl, "/sr/sr_sendung_overview_page_empty.html");

      final Set<SrTopicUrlDTO> actual = executeTask("Test", requestUrl);

      assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  private Set<SrTopicUrlDTO> executeTask(String aTheme, String aRequestUrl) {
      return new SrTopicArchivePageTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl))
              .invoke();
  }
}
