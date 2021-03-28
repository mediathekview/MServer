package de.mediathekview.mserver.crawler.hr.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.hr.HrCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
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

public class HrSendungsfolgenOverviewPageTaskTest extends HrTaskTestBase {

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Mock JsoupConnection jsoupConnection;

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/index.html";
    jsoupConnection = JsoupMock.mock(requestUrl, "/hr/hr_topic_page1.html");
    HrCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44522.html"),
          new CrawlerUrlDTO(
              "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44382.html"),
          new CrawlerUrlDTO(
              "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44440.html"),
          new CrawlerUrlDTO(
              "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44458.html")
        };

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgenOverviewPageTask target =
        new HrSendungsfolgenOverviewPageTask(crawler, urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  public void testHessenschau() throws IOException {
    final String requestUrl = "https://www.hessenschau.de/tv-sendung/sendungsarchiv/index.html";
    jsoupConnection = JsoupMock.mock(requestUrl, "/hr/hr_topic_page_hessenschau.html");
    HrCrawler crawler = createCrawler();
    crawler.setConnection(jsoupConnection);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74734.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74630.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74570.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74474.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74398.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74260.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74126.html"),
          new CrawlerUrlDTO(
              "https://www.hessenschau.de/tv-sendung/hessenschau---ganze-sendung,video-74034.html")
        };

    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgenOverviewPageTask target =
        new HrSendungsfolgenOverviewPageTask(crawler, urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
