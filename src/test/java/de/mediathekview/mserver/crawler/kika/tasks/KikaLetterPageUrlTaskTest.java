package de.mediathekview.mserver.crawler.kika.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.kika.KikaConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore({
  "com.sun.org.apache.xerces.*",
  "javax.xml.*",
  "org.xml.*",
  "org.w3c.*",
  "com.sun.org.apache.xalan.*",
  "javax.net.ssl.*"
})
public class KikaLetterPageUrlTaskTest extends KikaTaskTestBase {

  public KikaLetterPageUrlTaskTest() {}

  @Test
  public void test() throws IOException {
    final String requestUrl = KikaConstants.URL_TOPICS_PAGE;
    JsoupMock.mock(requestUrl, "/kika/kika_letter_pageA.html");

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
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
        new KikaLetterPageUrlTask(createCrawler(), urls, KikaConstants.BASE_URL);
    final Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }
}
