package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.eclipse.jetty.util.IO;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class KikaSendungVerpasstTaskTest extends KikaTaskTestBase {

  @Mock
  JsoupConnection jsoupConnection;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl =
        "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html";
    Connection connection = JsoupMock.mock(requestUrl, "/kika/kika_days_page1_no_before_after.html");
    when(jsoupConnection.getConnection(eq(requestUrl))).thenReturn(connection);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO("https://www.kika.de/rocket-ich/sendungen/sendung41156.html"),
          new CrawlerUrlDTO("https://www.kika.de/rocket-ich/sendungen/sendung41184.html")
        };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaSendungVerpasstTask target =
        new KikaSendungVerpasstTask(createCrawler(), urls, wireMockServer.baseUrl(), jsoupConnection);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void testLoadBeforeAndAfter() {
    final String requestUrl =
        "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-8f00c70b.html";

    final Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(requestUrl, "/kika/kika_days_page2_initial.html");
    urlMapping.put(
        wireMockServer.baseUrl()
            + "/sendungen/ipg/ipg102-beforeEntries_date-10032019_min-1105_zc-c7c340da.html",
        "/kika/kika_days_page2_before.html");
    urlMapping.put(
        wireMockServer.baseUrl()
            + "/sendungen/ipg/ipg102-afterEntries_date-10032019_max-1555_zc-8b42826a.html",
        "/kika/kika_days_page2_after.html");

    Map<String, Connection> resultMap = JsoupMock.mock(urlMapping);

    resultMap.forEach((currentUrl, currentResultConnection) -> {
      try {
        when(jsoupConnection.getConnection(currentUrl)).thenReturn(currentResultConnection);
      } catch (IOException ioe) {
        fail();
      }

    });

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final KikaSendungVerpasstTask target =
        new KikaSendungVerpasstTask(createCrawler(), urls, wireMockServer.baseUrl(), jsoupConnection);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(15));
  }
}
