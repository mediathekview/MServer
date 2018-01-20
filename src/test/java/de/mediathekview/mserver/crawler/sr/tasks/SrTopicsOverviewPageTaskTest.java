package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.sr.SrConstants;
import de.mediathekview.mserver.testhelper.JsoupMock;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import org.jsoup.Jsoup;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Jsoup.class})
public class SrTopicsOverviewPageTaskTest {
  
  private final CrawlerUrlDTO[] expectedUrls = new CrawlerUrlDTO[] {
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MA", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_ME_P", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MT", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "AS_MEZI", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "MHAH", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_MK", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "SR2_MUW", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "NIES_A", 1)),
    new CrawlerUrlDTO(String.format(SrConstants.URL_SHOW_ARCHIVE_PAGE, "ZMANN", 1))
  };
  
  @Test
  public void test() throws Exception {
    SrTopicsOverviewPageTask target = new SrTopicsOverviewPageTask();
    
    Map<String, String> urlMapping = new HashMap<>();
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE, "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "def", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "ghi", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "jkl", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "mno", "/sr/sr_overview_mno.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "pqr", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "stu", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "vwxyz", "/sr/sr_overview_empty.html");
    urlMapping.put(SrConstants.URL_OVERVIEW_PAGE + "ziffern", "/sr/sr_overview_09.html");

    JsoupMock.mock(urlMapping);
    
    ConcurrentLinkedQueue<CrawlerUrlDTO> actual = target.call();
    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }  
}
