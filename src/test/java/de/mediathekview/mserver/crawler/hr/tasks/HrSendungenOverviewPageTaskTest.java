package de.mediathekview.mserver.crawler.hr.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.io.IOException;
import java.util.Set;
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
public class HrSendungenOverviewPageTaskTest extends HrTaskTestBase {

  @Test
  public void test() throws IOException {
    final String requestUrl = "http://localhost:8589/sendungen-a-z/index.html";
    JsoupMock.mock(requestUrl, "/hr/hr_topics_page.html");

    setupHeadResponse("/sendungen-a-z/alle-wetter/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/alles-wissen/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/wer-weiss-es/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/wilde-camper/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/besuch-mich/sendungen/index.html", 404);
    setupHeadResponse("/sendungen-a-z/hr-katzen/sendungen/index.html", 404);

    final CrawlerUrlDTO[] expected = new CrawlerUrlDTO[]{
        new CrawlerUrlDTO(
            "http://localhost:8589/sendungen-a-z/alle-wetter/sendungen/index.html"),
        new CrawlerUrlDTO(
            "http://localhost:8589/sendungen-a-z/alles-wissen/sendungen/index.html"),
        new CrawlerUrlDTO(
            "http://localhost:8589/sendungen-a-z/besuch-mich/index.html"),
        new CrawlerUrlDTO(
            "https://www.hessenschau.de/tv-sendung/sendungsarchiv/index.html"),
        new CrawlerUrlDTO(
            "http://localhost:8589/sendungen-a-z/hr-katzen/index.html"),
        new CrawlerUrlDTO(
            "http://localhost:8589/sendungen-a-z/wer-weiss-es/sendungen/index.html"),
        new CrawlerUrlDTO(
            "http://localhost:8589/sendungen-a-z/wilde-camper/sendungen/index.html"),
    };

    final HrSendungenOverviewPageTask target = new HrSendungenOverviewPageTask(WireMockTestBase.MOCK_URL_BASE + "/");
    final Set<CrawlerUrlDTO> actual = target.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }

}