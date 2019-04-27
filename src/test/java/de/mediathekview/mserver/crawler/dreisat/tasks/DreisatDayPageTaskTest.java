package de.mediathekview.mserver.crawler.dreisat.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;

public class DreisatDayPageTaskTest extends DreisatTaskTestBase {

  @Test
  public void testDayFirstPageWithSubpages() {
    final String requestUrl = "/mediathek/?datum=20190427&cx=171";
    setupSuccessfulResponse(requestUrl, "/dreisat/dreisat_day_page1.html");
    setupSuccessfulResponse("/mediathek/?datum=20190427&cx=171&mode=verpasst1", "/dreisat/dreisat_day_page_last.html");
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual.size(), equalTo(19));
  }

  private Set<CrawlerUrlDTO> executeTask(String aRequestUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + aRequestUrl));
    return new DreisatDayPageTask(
        createCrawler(), urls, false)
        .invoke();
  }
}