package de.mediathekview.mserver.crawler.sr.tasks;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.hamcrest.Matchers;
import org.jsoup.nodes.Document;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class SrTopicsOverviewPageTaskTest {

  private final SrTopicUrlDTO[] expectedUrls =
      new SrTopicUrlDTO[] {
        new SrTopicUrlDTO("mag's", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MA", 1)),
        new SrTopicUrlDTO(
            "MedienWelt", String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_ME_P", 1)),
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
  void test() throws Exception {
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
            when(crawler.requestBodyAsHtmlDocument(url)).thenReturn(document);
          } catch (final IOException iox) {
            fail();
          }
        });

    final SrTopicsOverviewPageTask target = new SrTopicsOverviewPageTask(crawler);
    final Queue<SrTopicUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
