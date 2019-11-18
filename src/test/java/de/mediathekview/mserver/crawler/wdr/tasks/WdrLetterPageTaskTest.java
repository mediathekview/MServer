package de.mediathekview.mserver.crawler.wdr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.wdr.WdrCrawler;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import org.hamcrest.Matchers;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class WdrLetterPageTaskTest {

  @Mock
  JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test() {
    final Map<String, String> mapping = new HashMap<>();
    mapping.put(
        "https://www1.wdr.de/mediathek/video/sendungen-a-z/index.html",
        "/wdr/wdr_letter_page1.html");
    mapping.put(
        "https://www1.wdr.de/mediathek/video/sendungen-a-z/sendungen-u-102.html",
        "/wdr/wdr_letter_page2.html");

    mapping.forEach((url, fileName) -> {
      try {
        Document document = JsoupMock.getFileDocument(url, fileName);
        when(jsoupConnection.getDocumentTimeoutAfter(eq(url), anyInt())).thenReturn(document);
      } catch (IOException iox) {
        fail();
      }
    });

    final WdrTopicUrlDto[] expected = {
        new WdrTopicUrlDto(
            "Quarks & Co",
            "https://www1.wdr.de/mediathek/video/sendungen/quarks-und-co/index.html",
            false),
        new WdrTopicUrlDto(
            "Das Quiz für den Westen",
            "https://www1.wdr.de/mediathek/video/sendungen/das-quiz-fuer-den-westen/index.html",
            false),
        new WdrTopicUrlDto(
            "Unterhaltung",
            "https://www1.wdr.de/mediathek/video/sendungen/unterhaltung/index.html",
            false),
        new WdrTopicUrlDto(
            "Unser Westen",
            "https://www1.wdr.de/mediathek/video/sendungen/unser-westen/index.html",
            false)
    };
    final WdrCrawler crawler = Mockito.mock(WdrCrawler.class);
    when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.WDR));
    final WdrLetterPageTask target = new WdrLetterPageTask(crawler, jsoupConnection);
    final Queue<WdrTopicUrlDto> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
