package de.mediathekview.mserver.crawler.tagesschau.tasks;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.EntryUrlDto;
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
    final Set<EntryUrlDto> actual = executeTask(requestUrl);
    assertThat(actual.size(), equalTo(1));
    final EntryUrlDto actualEntry = actual.iterator().next();
    assertThat(actualEntry.getSubPages().size(), equalTo(0));
    assertThat(actualEntry.getVideos().size(), equalTo(expectedUrls.length));
    assertThat(actualEntry.getVideos(), Matchers.containsInAnyOrder(expectedUrls));
  }


  @Test
  void testYear() {
    final String requestUrl = "http://tagesschau-year.de";
    final CrawlerUrlDTO[] expectedUrls =
            new CrawlerUrlDTO[] {
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-476.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-474.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-468.html")
            };

    jsoupConnection =
            JsoupMock.mock(
                    requestUrl, "/tagesschau/tagesschau_20jahre_year.html");

    final Set<EntryUrlDto> actual = executeTask(requestUrl);
    assertThat(actual.size(), equalTo(1));
    final EntryUrlDto actualEntry = actual.iterator().next();
    assertThat(actualEntry.getSubPages().size(), equalTo(expectedUrls.length));
    assertThat(actualEntry.getVideos().size(), equalTo(0));
    assertThat(actualEntry.getSubPages(), Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  void testOverview() {
    final String requestUrl = "http://tagesschau-overview.de";
    final CrawlerUrlDTO[] expectedUrls =
            new CrawlerUrlDTO[] {
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-478.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-472.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-442.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-416.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/multimedia/tsvorzwanzigjahren-387.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-359.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-327.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-301.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-257.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-221.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-183.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-147.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-121.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-136.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-116.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-100.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-106.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-104.html"),
                    new CrawlerUrlDTO("https://www.tagesschau.de/inland/tsvorzwanzigjahren-ts-102.html")
            };

    jsoupConnection =
            JsoupMock.mock(
                    requestUrl, "/tagesschau/tagesschau_20jahre_overview.html");

    final Set<EntryUrlDto> actual = executeTask(requestUrl);
    assertThat(actual.size(), equalTo(1));
    final EntryUrlDto actualEntry = actual.iterator().next();
    assertThat(actualEntry.getSubPages().size(), equalTo(expectedUrls.length));
    assertThat(actualEntry.getVideos().size(), equalTo(0));
    assertThat(actualEntry.getSubPages(), Matchers.containsInAnyOrder(expectedUrls));
  }

  private Set<EntryUrlDto> executeTask(String requestUrl) {
    final TagesschauCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final ConcurrentLinkedQueue<CrawlerUrlDTO> queue = new ConcurrentLinkedQueue<>();
    queue.add(new CrawlerUrlDTO(requestUrl));

    final TagesschauEntriesTask target = new TagesschauEntriesTask(crawler, queue);
    return target.invoke();
  }

}
