package de.mediathekview.mserver.crawler.srf.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(Parameterized.class)
public class SrfSendungenOverviewPageTaskTest {

  private final String htmlFile;
  private final int expectedUrls;

  @Mock
  JsoupConnection jsoupConnection;

  @Mock
  SrfCrawler crawler;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

    when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.SRF));

  }

  public SrfSendungenOverviewPageTaskTest(final String aHtmlFile, final int aExpectedUrls) {
    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][]{
            {"/srf/srf_overview_page_no_data_attribute.htm", 0},
            {"/srf/srf_overview_page_no_div_with_class.htm", 0},
            {"/srf/srf_overview_page.htm", 162},
        });
  }

  @Test
  public void test() throws Exception {

    Document document = JsoupMock.getFileDocument(SrfConstants.OVERVIEW_PAGE_URL, htmlFile);
    when(jsoupConnection.getDocumentTimeoutAfter(eq(SrfConstants.OVERVIEW_PAGE_URL), anyInt())).thenReturn(document);

    SrfSendungenOverviewPageTask target = new SrfSendungenOverviewPageTask(crawler, jsoupConnection);
    final ConcurrentLinkedQueue<CrawlerUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedUrls));
  }
}
