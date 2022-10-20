package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.funk.json.FunkChannelDeserializer;
import de.mediathekview.mserver.crawler.funk.tasks.FunkChannelsRestTask;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestEndpoint;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class FunkChannelsRestTaskTest extends FunkTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() {
    final String requestUrl = "/api/v4.0/channels/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_channel_page_last.json");

    final Set<FunkChannelDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(3));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getSenderConfig(Sender.FUNK).setMaximumSubpages(5);

    final String requestUrl = "/api/v4.0/channels/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_channel_page1_1.json");
    setupSuccessfulJsonResponse(
            "/api/v4.0/channels/?page=1&size=10&sort=updateDate,desc",
            "/funk/funk_channel_page1_2.json");
    setupSuccessfulJsonResponse(
            "/api/v4.0/channels/?page=2&size=10&sort=updateDate,desc",
            "/funk/funk_channel_page1_3.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/channels/?page=3&size=10&sort=updateDate,desc",
        "/funk/funk_channel_page1_last.json");

    final Set<FunkChannelDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(33));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.FUNK).setMaximumSubpages(3);

    final String requestUrl = "/api/v4.0/channels/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_channel_page1_1.json");
    setupSuccessfulJsonResponse(
        "/api/v4.0/channels/?page=1&size=10&sort=updateDate,desc",
        "/funk/funk_channel_page1_2.json");
    setupSuccessfulJsonResponse(
            "/api/v4.0/channels/?page=2&size=10&sort=updateDate,desc",
            "/funk/funk_channel_page1_3.json");
    setupSuccessfulJsonResponse(
            "/api/v4.0/channels/?page=3&size=10&sort=updateDate,desc",
            "/funk/funk_channel_page1_last.json");

    final Set<FunkChannelDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(33));
  }

  @Test
  public void testOverviewPageNotFound() {
    final String requestUrl = "/api/v4.0/channels/";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<FunkChannelDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<FunkChannelDTO> executeTask(final String aRequestUrl) {
    final FunkCrawler crawler = createCrawler();
    return new FunkChannelsRestTask(
            crawler,
            new FunkRestEndpoint<>(
                FunkApiUrls.CHANNELS, new FunkChannelDeserializer(Optional.of(crawler))),
            createCrawlerUrlDto(aRequestUrl))
        .invoke();
  }
}
