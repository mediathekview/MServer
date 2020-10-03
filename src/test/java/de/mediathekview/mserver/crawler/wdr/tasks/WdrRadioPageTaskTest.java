package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.wdr.WdrConstants;
import de.mediathekview.mserver.crawler.wdr.WdrTopicUrlDto;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class WdrRadioPageTaskTest extends WdrTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = WdrConstants.URL_RADIO_WDR4;
    final Connection connection = JsoupMock.mock(requestUrl, "/wdr/wdr4_overview.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final WdrTopicUrlDto[] expected =
        new WdrTopicUrlDto[] {
          new WdrTopicUrlDto(
              "WDR 4 Events",
              "https://www1.wdr.de/mediathek/video/radio/wdr4/wdr4-videos-events-100.html",
              false),
          new WdrTopicUrlDto(
              "Kuttler Digital",
              "https://www1.wdr.de/mediathek/video/radio/wdr4/wdr4-videos-kuttler-digital-100.html",
              false),
          new WdrTopicUrlDto(
              "Ullas Lieblingsrezepte",
              "https://www1.wdr.de/mediathek/video/radio/wdr4/wdr4-videos-ullas-lieblingsrezepte-100.html",
              false),
          new WdrTopicUrlDto(
              "WDR 4 Studiogäste",
              "https://www1.wdr.de/mediathek/video/radio/wdr4/wdr4-videos-studiogaeste-102.html",
              false),
          new WdrTopicUrlDto(
              "WDR 4 Aktionen",
              "https://www1.wdr.de/mediathek/video/radio/wdr4/wdr4-videos-aktionen-100.html",
              false),
        };

    final Queue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));

    final WdrRadioPageTask target = new WdrRadioPageTask(createCrawler(), queue, jsoupConnection);
    final Set<WdrTopicUrlDto> actual = target.invoke();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
