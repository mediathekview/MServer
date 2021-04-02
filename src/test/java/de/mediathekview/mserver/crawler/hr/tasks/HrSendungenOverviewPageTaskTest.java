package de.mediathekview.mserver.crawler.hr.tasks;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.hamcrest.Matchers;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;

import java.io.IOException;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

public class HrSendungenOverviewPageTaskTest extends HrTaskTestBase {

  @Mock JsoupConnection jsoupConnection;

  @InjectMocks @Spy
  HrSendungenOverviewPageTask classUnderTest =
      new HrSendungenOverviewPageTask(getWireMockBaseUrlSafe() + "/", createCrawler());

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  public void test() throws IOException {
    final String requestUrl = getWireMockBaseUrlSafe() + "/sendungen-a-z/index.html";

    setupHeadResponse("/sendungen-a-z/alle-wetter/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/alles-wissen/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/wer-weiss-es/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/wilde-camper/sendungen/index.html", 200);
    setupHeadResponse("/sendungen-a-z/besuch-mich/sendungen/index.html", 404);
    setupHeadResponse("/sendungen-a-z/hr-katzen/sendungen/index.html", 404);

    final CrawlerUrlDTO[] expected =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO(
              getWireMockBaseUrlSafe() + "/sendungen-a-z/alle-wetter/sendungen/index.html"),
          new CrawlerUrlDTO(
              getWireMockBaseUrlSafe() + "/sendungen-a-z/alles-wissen/sendungen/index.html"),
          new CrawlerUrlDTO(getWireMockBaseUrlSafe() + "/sendungen-a-z/besuch-mich/index.html"),
          new CrawlerUrlDTO("https://www.hessenschau.de/tv-sendung/sendungsarchiv/index.html"),
          new CrawlerUrlDTO(getWireMockBaseUrlSafe() + "/sendungen-a-z/hr-katzen/index.html"),
          new CrawlerUrlDTO(
              getWireMockBaseUrlSafe() + "/sendungen-a-z/wer-weiss-es/sendungen/index.html"),
          new CrawlerUrlDTO(
              getWireMockBaseUrlSafe() + "/sendungen-a-z/wilde-camper/sendungen/index.html"),
        };

    final Document document = JsoupMock.getFileDocument(requestUrl, "/hr/hr_topics_page.html");
    when(jsoupConnection.getDocumentTimeoutAfter(eq(requestUrl), anyInt())).thenReturn(document);

    final Set<CrawlerUrlDTO> actual = classUnderTest.call();

    assertThat(actual.size(), equalTo(expected.length));
    assertThat(actual, Matchers.containsInAnyOrder(expected));
  }
}
