package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.funk.json.FunkVideoDeserializer;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestEndpoint;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestTask;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class FunkVideoRestTaskTest extends FunkTaskTestBase {

  @Test
  void testOverviewWithSinglePage() {
    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_video_page_last.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual).isNotNull().hasSize(3);
  }

  @Test
  void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getSenderConfig(Sender.FUNK).setMaximumSubpages(5);

    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_video_page_1.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=1&size=20&sort=updateDate,desc", "/funk/funk_video_page_2.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=2&size=20&sort=updateDate,desc", "/funk/funk_video_page_3.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=3&size=20&sort=updateDate,desc", "/funk/funk_video_page_last.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual).isNotNull().hasSize(63);
  }

  @Test
  void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.FUNK).setMaximumSubpages(2);

    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_video_page_1.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=1&size=20&sort=updateDate,desc", "/funk/funk_video_page_2.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=2&size=20&sort=updateDate,desc", "/funk/funk_video_page_3.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=3&size=20&sort=updateDate,desc", "/funk/funk_video_page_last.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual).isNotNull().hasSize(40);
  }

  @Test
  void testOverviewPageNotFound() {
    final String requestUrl = "/api/v4.0/videos/";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual).isNotNull().isEmpty();
  }

  private Set<FilmInfoDto> executeTask(final String aRequestUrl) {
    final FunkCrawler crawler = createCrawler();
    return new FunkRestTask<>(
            crawler,
            new FunkRestEndpoint<>(FunkApiUrls.VIDEOS, new FunkVideoDeserializer(crawler)),
            createCrawlerUrlDto(aRequestUrl))
        .invoke();
  }
}
