package de.mediathekview.mserver.crawler.dreisat.tasks;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Test;

public class DreisatTopicsOverviewPageTaskTest extends DreisatTaskTestBase {

  @Test
  public void testTopicFirstPageWithSubpages() {
    final String requestUrl = "/mediathek/?mode=sendungenaz";
    setupSuccessfulResponse(requestUrl, "/dreisat/dreisat_topics_overview_page1.html");
    setupSuccessfulResponse(requestUrl + "1", "/dreisat/dreisat_topics_overview_page_last.html");

    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual.size(), equalTo(22));
  }

  private Set<CrawlerUrlDTO> executeTask(String aRequestUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(WireMockTestBase.MOCK_URL_BASE + aRequestUrl));
    return new DreisatTopicsOverviewPageTask(
        createCrawler(), urls, false)
        .invoke();
  }
}