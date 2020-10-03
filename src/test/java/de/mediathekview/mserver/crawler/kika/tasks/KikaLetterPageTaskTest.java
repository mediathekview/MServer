package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KikaLetterPageTaskTest extends KikaTaskTestBase {

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Mock JsoupConnection jsoupConnection;

  public KikaLetterPageTaskTest() {}

  @Test
  public void test() throws IOException {
    final String requestUrl =
        "https://www.kika.de/sendungen/sendungenabisz100_page-V_zc-1fc26dc3.html";
    final Connection connection = JsoupMock.mock(requestUrl, "/kika/kika_letter_pageV.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO("https://www.kika.de/verbotene-geschichten/sendereihe290.html"),
          new CrawlerUrlDTO("https://www.kika.de/verknallt-abgedreht/sendereihe2128.html"),
          new CrawlerUrlDTO("https://www.kika.de/vier-kartoffeln/sendereihe2124.html")
        };

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaLetterPageTask target =
        new KikaLetterPageTask(createCrawler(), urls, KikaConstants.BASE_URL, jsoupConnection);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
