package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import org.junit.Test;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

public class SrfTopicOverviewTaskTest extends SrfTaskTestBase {

  private Set<CrawlerUrlDTO> executeTask() {
    final Queue<TopicUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(
        new TopicUrlDTO(
            "1",
            getWireMockBaseUrlSafe() + "/play/v3/api/srf/production/videos-by-show-id?showId=1"));
    return new SrfTopicOverviewTask(createCrawler(), urls, getWireMockBaseUrlSafe()).invoke();
  }

  @Test
  public void testOverviewWithSinglePage() {
    final String requestUrl = "/play/v3/api/srf/production/videos-by-show-id?showId=1";
    setupSuccessfulJsonResponse(requestUrl, "/srf/srf_topic_page1.json");

    final Set<CrawlerUrlDTO> actual = executeTask();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(4));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.SRF).setMaximumSubpages(1);

    final String requestUrl = "/play/v3/api/srf/production/videos-by-show-id?showId=1";
    setupSuccessfulJsonResponse(requestUrl, "/srf/srf_topic_page_with_next.json");

    final Set<CrawlerUrlDTO> actual = executeTask();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(2));
  }
}
