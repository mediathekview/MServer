package de.mediathekview.mserver.crawler.ard.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.ard.ArdFilmInfoDto;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.junit.Test;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ArdTopicPageTaskTest extends ArdTaskTestBase {

  @Test
  public void testNoPagination() {
    rootConfig.getSenderConfig(Sender.ARD).setMaximumSubpages(5);

    final String filmUrl =
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3JhZGlvYnJlbWVuLmRlLzNuYWNoOQ?pageSize=5";
    setupSuccessfulJsonResponse(filmUrl, "/ard/ard_topic.json");

    final Set<ArdFilmInfoDto> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(5));
  }

  @Test
  public void testPaginationLoadOnlyOneSubpage() {
    rootConfig.getSenderConfig(Sender.ARD).setMaximumSubpages(1);

    final String filmUrl =
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15";
    setupSuccessfulJsonResponse(filmUrl, "/ard/ard_topic_page0.json");
    setupSuccessfulJsonResponse(
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15&pageNumber=1",
        "/ard/ard_topic_page1.json");
    setupSuccessfulJsonResponse(
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15&pageNumber=2",
        "/ard/ard_topic_page2.json");

    final Set<ArdFilmInfoDto> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(30));
  }

  @Test
  public void testPaginationLoadAllWithMaxSubpagesHigherThanPageCount() {
    rootConfig.getSenderConfig(Sender.ARD).setMaximumSubpages(10);

    final String filmUrl =
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15";
    setupSuccessfulJsonResponse(filmUrl, "/ard/ard_topic_page0.json");
    setupSuccessfulJsonResponse(
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15&pageNumber=1",
        "/ard/ard_topic_page1.json");
    setupSuccessfulJsonResponse(
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15&pageNumber=2",
        "/ard/ard_topic_page2.json");
    setupSuccessfulJsonResponse(
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15&pageNumber=3",
        "/ard/ard_topic_page3.json");
    // this call should never be done. If it is done, the result size will be to high
    setupSuccessfulJsonResponse(
        "/page-gateway/widgets/ard/asset/Y3JpZDovL3N3ci5kZS9zZGIvc3RJZC8xMzM3?pageSize=15&pageNumber=4",
        "/ard/ard_topic_page3.json");

    final Set<ArdFilmInfoDto> actual = executeTask(filmUrl);

    assertThat(actual.size(), equalTo(57));
  }

  private Set<ArdFilmInfoDto> executeTask(final String aDetailUrl) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    urls.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + aDetailUrl));
    return new ArdTopicPageTask(createCrawler(), urls).invoke();
  }
}
