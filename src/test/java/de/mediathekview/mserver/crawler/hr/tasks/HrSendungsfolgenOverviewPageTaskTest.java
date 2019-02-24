package de.mediathekview.mserver.crawler.hr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(value= {"javax.net.ssl.*", "javax.*", "com.sun.*", "org.apache.logging.log4j.core.config.xml.*"})
public class HrSendungsfolgenOverviewPageTaskTest extends HrTaskTestBase {
  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/index.html";
    JsoupMock.mock(requestUrl, "/hr/hr_topic_page1.html");

    final CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44522.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44382.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44440.html"),
        new CrawlerUrlDTO(
            "https://www.hr-fernsehen.de/sendungen-a-z/besuch-mich/sendungen/besuch-mich-wer-ist-hessens-bester-gastgeber,sendung-44458.html")
    };

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgenOverviewPageTask target = new HrSendungsfolgenOverviewPageTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

  @Test
  public void testHessenschau() throws IOException {
    final String requestUrl = "https://www.hessenschau.de/tv-sendung/sendungsarchiv/index.html";
    JsoupMock.mock(requestUrl, "/hr/hr_topic_page_hessenschau.html");

    final CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
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

    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    final HrSendungsfolgenOverviewPageTask target = new HrSendungsfolgenOverviewPageTask(createCrawler(), urls);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}