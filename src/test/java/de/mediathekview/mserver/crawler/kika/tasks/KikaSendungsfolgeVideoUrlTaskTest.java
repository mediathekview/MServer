package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
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
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class KikaSendungsfolgeVideoUrlTaskTest extends KikaTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.kika.de/rocket-ich/sendungen/sendung41184.html";
    final Connection connection = JsoupMock.mock(requestUrl, "/kika/kika_film1.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.kika.de/rocket-ich/sendungen/videos/video14406-avCustom.xml")
        };

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaSendungsfolgeVideoUrlTask target =
        new KikaSendungsfolgeVideoUrlTask(createCrawler(), urls, jsoupConnection);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
