package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;

public class SrTopicsOverviewPageTaskTest {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  private final SrTopicUrlDTO[] expectedUrls =
      new SrTopicUrlDTO[] {
        new SrTopicUrlDTO("mag's", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MA", 1)),
        new SrTopicUrlDTO(
            "Medienwelt", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_ME_P", 1)),
        new SrTopicUrlDTO(
            "Meine Traumreise", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MT", 1)),
        new SrTopicUrlDTO(
            "mezz'ora italiana", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "AS_MEZI", 1)),
        new SrTopicUrlDTO(
            "Mit Herz am Herd", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MHAH", 1)),
        new SrTopicUrlDTO(
            "MusikKompass", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_MK", 1)),
        new SrTopicUrlDTO(
            "MusikWelt", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_MUW", 1)),
        new SrTopicUrlDTO(
            "Nachrichten in einfacher Sprache",
            String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "NIES_A", 1)),
        new SrTopicUrlDTO(
            "2 Mann für alle Gänge", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "ZMANN", 1))
      };

  @Test
  public void test() throws Exception {
    final SrCrawler crawler = Mockito.mock(SrCrawler.class);

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE, "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "def", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "ghi", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "jkl", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "mno", "/sr/sr_overview_mno.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "pqr", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "stu", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "vwxyz", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "ziffern", "/sr/sr_overview_09.html");

    urlMapping.forEach(
        (url, fileName) -> {
          try {
            final Document document = JsoupMock.getFileDocument(fileName);
            when(jsoupConnection.requestBodyAsHtmlDocument(url)).thenReturn(document);
            when(crawler.requestBodyAsHtmlDocument(url)).thenReturn(document);
          } catch (final IOException iox) {
            fail();
          }
        });

    when(crawler.getCrawlerConfig())
        .thenReturn(
            new MServerConfigManager("MServer-JUnit-Config.yaml").getSenderConfig(Sender.SR));
    when(crawler.getConnection()).thenReturn(jsoupConnection);

    final SrTopicsOverviewPageTask target = new SrTopicsOverviewPageTask(crawler);
    final Queue<SrTopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
