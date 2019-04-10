package de.mediathekview.mserver.crawler.kika.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore("javax.net.ssl.*")
public class KikaSendungVerpasstTaskTest extends KikaTaskTestBase {

  @Test
  public void test() throws IOException {
    final String requestUrl = "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-09032019_zc-8f00c70b.html";
    JsoupMock.mock(requestUrl, "/kika/kika_days_page1_no_before_after.html");

    CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO("https://www.kika.de/rocket-ich/sendungen/sendung41156.html"),
        new CrawlerUrlDTO("https://www.kika.de/rocket-ich/sendungen/sendung41184.html")
    };

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaSendungVerpasstTask target = new KikaSendungVerpasstTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, containsInAnyOrder(expected));
  }

  @Test
  public void testLoadBeforeAndAfter() {
    final String requestUrl = "https://www.kika.de/sendungen/ipg/ipg102-initialEntries_date-10032019_zc-8f00c70b.html";

    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(requestUrl, "/kika/kika_days_page2_initial.html");
    urlMapping.put(WireMockTestBase.MOCK_URL_BASE + "/sendungen/ipg/ipg102-beforeEntries_date-10032019_min-1105_zc-c7c340da.html",
        "/kika/kika_days_page2_before.html");
    urlMapping.put(WireMockTestBase.MOCK_URL_BASE + "/sendungen/ipg/ipg102-afterEntries_date-10032019_max-1555_zc-8b42826a.html",
        "/kika/kika_days_page2_after.html");

    JsoupMock.mock(urlMapping);

    ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(requestUrl));

    KikaSendungVerpasstTask target = new KikaSendungVerpasstTask(createCrawler(), urls, WireMockTestBase.MOCK_URL_BASE);
    Set<CrawlerUrlDTO> actual = target.invoke();

    assertThat(actual.size(), equalTo(15));
  }
}