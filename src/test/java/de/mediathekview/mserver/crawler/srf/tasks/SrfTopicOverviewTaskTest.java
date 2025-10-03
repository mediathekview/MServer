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
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(2);

    final String requestUrl = "/play/v3/api/srf/production/videos-by-show-id?showId=1";
    setupSuccessfulJsonResponse(requestUrl, "/srf/srf_topic_page_with_next.json");
    setupSuccessfulJsonResponse(
        "/play/v3/api/srf/production/videos-by-show-id?showId=1&next=09e12b6f403c2da8bfde15a1c99070d421a97f0c37210dbda272ccf5a386e3099d6a236a2a96941b339f13d41a24eeb58d4a7c6cbc5220170ed2ecbc81f080465303ba40356017efbefb7efe95be9f1ef63b0216702f1b09a6094e6a7d2631f9f40f0d2e1ca431002db278684a8e97b7a82219c4c83769ba9ad8dd65d4c6f19620b837f9a1ef66fda473f7212a326361da7e4b3b8386668475ef72e3ad347396d147878b2d7408856a23cf20af8180946bf36c00af3044485321237fa36db791a80f46df2744fd825ddf82fba29e6f0e02e70e5c4d0aa422de2aa86c7e3bf98e0502fcbae23341621e5edde21c9471f8f3ed52b5ce30a6dfa66b2c4aca2ff51ea7ed010bf147e87253a7a5123b8e0ba835ebeb15d27362d1748f0e6287c845818e03431d2a13d2bcad2e2b40ed63a03bb5ed2a5a64c874a2",
        "/srf/srf_topic_page_last.json");

    final Set<CrawlerUrlDTO> actual = executeTask();

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(5));
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
