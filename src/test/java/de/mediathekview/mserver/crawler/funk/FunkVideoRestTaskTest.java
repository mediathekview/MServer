package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.FilmInfoDto;
import de.mediathekview.mserver.crawler.funk.json.FunkVideoDeserializer;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestEndpoint;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestTask;
import org.junit.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class FunkVideoRestTaskTest extends FunkTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() {
    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_video_page_last.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(3));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getSenderConfig(Sender.FUNK).setMaximumSubpages(5);

    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_video_page_1.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=1&size=100&sort=updateDate,desc",
        "/funk/funk_video_page_last.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(103));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.FUNK).setMaximumSubpages(1);

    final String requestUrl = "/api/v4.0/videos/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_video_page_1.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/videos/?page=1&size=100&sort=updateDate,desc",
        "/funk/funk_video_page_last.json");

    final Set<FilmInfoDto> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(100));
  }

  @Test
  public void testOverviewPageNotFound() {
    final String requestUrl = "/api/v4.0/videos/";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<FilmInfoDto> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<FilmInfoDto> executeTask(final String aRequestUrl) {
    final FunkCrawler crawler = createCrawler();
    return new FunkRestTask<>(
            crawler,
            new FunkRestEndpoint<>(
                FunkApiUrls.VIDEOS, new FunkVideoDeserializer(crawler)),
            createCrawlerUrlDto(aRequestUrl))
        .invoke();
  }
}
