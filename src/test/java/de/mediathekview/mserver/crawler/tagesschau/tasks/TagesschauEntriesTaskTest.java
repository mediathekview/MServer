package de.mediathekview.mserver.crawler.tagesschau.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.TagesschauCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TagesschauEntriesTaskTest extends TagesschauTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testMonth() {
    final String requestUrl = "http://tagesschau-month.de";
    final CrawlerUrlDTO[] expectedUrls =
            new CrawlerUrlDTO[] {
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547694.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547686.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547682.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547680.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1547676.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1539874.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1539872.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/sendung/tagesschau_vor_20_jahren/video-1539870.html")
            };

    jsoupConnection =
            JsoupMock.mock(
                    requestUrl, "/tagesschau/tagesschau_20jahre_month.html");
    final TagesschauCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));

    final TagesschauEnriesTask target = new TagesschauEnriesTask(crawler, queue);
    final Set<CrawlerUrlDTO> actual = target.invoke();
    assertEquals(expectedUrls.length, actual.size());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }
}
