package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.testhelper.JsoupMock;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.mockito.Mockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({Jsoup.class})
@PowerMockIgnore(
    value = {
      "javax.net.ssl.*",
      "javax.*",
      "com.sun.*",
      "org.apache.logging.log4j.core.config.xml.*"
    })
public class SrfSendungenOverviewPageTaskTest {

  private final String htmlFile;
  private final int expectedUrls;
  private final SrfSendungenOverviewPageTask target;

  public SrfSendungenOverviewPageTaskTest(final String aHtmlFile, final int aExpectedUrls) {
    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
    final SrfCrawler crawler = Mockito.mock(SrfCrawler.class);
    Mockito.when(crawler.getCrawlerConfig())
        .thenReturn(MServerConfigManager.getInstance().getSenderConfig(Sender.SRF));
    target = new SrfSendungenOverviewPageTask(crawler);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {"/srf/srf_overview_page_no_data_attribute.htm", 0},
          {"/srf/srf_overview_page_no_div_with_class.htm", 0},
          {"/srf/srf_overview_page.htm", 162},
        });
  }

  @Test
  public void test() throws Exception {

    JsoupMock.mock(SrfConstants.OVERVIEW_PAGE_URL, htmlFile);
    final ConcurrentLinkedQueue<CrawlerUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedUrls));
  }
}
