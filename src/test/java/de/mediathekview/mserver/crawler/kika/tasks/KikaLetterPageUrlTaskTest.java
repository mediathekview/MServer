package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KikaLetterPageUrlTaskTest extends KikaTaskTestBase {

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Mock
  JsoupConnection jsoupConnection;

  public KikaLetterPageUrlTaskTest() {
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = KikaConstants.URL_TOPICS_PAGE;
    Connection connection = JsoupMock.mock(requestUrl, "/kika/kika_letter_pageA.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[]{
            new CrawlerUrlDTO(
                "https://www.kika.de/sendungen/sendungenabisz100_page-A_zc-05fb1331.html"),
            new CrawlerUrlDTO(
                "https://www.kika.de/sendungen/sendungenabisz100_page-Q_zc-2cb019d6.html"),
            new CrawlerUrlDTO(
                "https://www.kika.de/sendungen/sendungenabisz100_page-V_zc-1fc26dc3.html"),
            new CrawlerUrlDTO(
                "https://www.kika.de/sendungen/sendungenabisz100_page-Y_zc-388beba7.html")
        };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaLetterPageUrlTask target =
        new KikaLetterPageUrlTask(createCrawler(), urls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
