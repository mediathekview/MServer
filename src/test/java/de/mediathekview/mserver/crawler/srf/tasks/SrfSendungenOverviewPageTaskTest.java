package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.jsoup.Jsoup;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunnerDelegate;
import static org.junit.Assert.assertThat;
import org.junit.runners.Parameterized;

@RunWith(PowerMockRunner.class)
@PowerMockRunnerDelegate(Parameterized.class)
@PrepareForTest({Jsoup.class})
public class SrfSendungenOverviewPageTaskTest {
  
  @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {  
            { "/srf/srf_overview_page_no_data_attribute.htm", 0 },
            { "/srf/srf_overview_page_no_div_with_class.htm", 0 },
            { "/srf/srf_overview_page.htm", 162 },
        });
  }

  private final String htmlFile;
  private final int expectedUrls;
  private final SrfSendungenOverviewPageTask target;
          
  public SrfSendungenOverviewPageTaskTest(String aHtmlFile, int aExpectedUrls) {
    htmlFile = aHtmlFile;
    expectedUrls = aExpectedUrls;
    
    target = new SrfSendungenOverviewPageTask();
  }
    
  @Test
  public void test() throws Exception {
    
    JsoupMock.mock(SrfConstants.OVERVIEW_PAGE_URL, htmlFile);
    ConcurrentLinkedQueue<CrawlerUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(expectedUrls));
  }
  

}
