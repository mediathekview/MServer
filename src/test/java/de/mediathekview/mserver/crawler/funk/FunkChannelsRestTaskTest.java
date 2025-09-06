package de.mediathekview.mserver.crawler.funk;

import com.google.gson.reflect.TypeToken;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.funk.json.FunkChannelDeserializer;
import de.mediathekview.mserver.crawler.funk.tasks.FunkChannelsRestTask;
import de.mediathekview.mserver.crawler.funk.tasks.FunkRestEndpoint;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;

class FunkChannelsRestTaskTest extends FunkTaskTestBase {

  @Test
  void testOverviewWithSinglePage() {
    final String requestUrl = "/api/v4.0/channels/";
    setupSuccessfulJsonResponse(requestUrl, "/funk/funk_channel_page_last.json");

    final Set<FunkChannelDTO> actual = executeTask(requestUrl);

    assertThat(actual).isNotNull().hasSize(3);
  }

  @Test
  void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

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

    assertThat(actual).isNotNull().hasSize(33);
  }

  @Test
  void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
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

    assertThat(actual).isNotNull().hasSize(33);
  }

  @Test
  void testOverviewPageNotFound() {
    final String requestUrl = "/api/v4.0/channels/";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<FunkChannelDTO> actual = executeTask(requestUrl);

    assertThat(actual).isNotNull().isEmpty();
  }

  private Set<FunkChannelDTO> executeTask(final String aRequestUrl) {
    final FunkCrawler crawler = createCrawler();
    return new FunkChannelsRestTask(
            crawler,
            new FunkRestEndpoint<>(
                FunkApiUrls.CHANNELS,
                new FunkChannelDeserializer(Optional.of(crawler)),
                new TypeToken<FunkChannelDTO>() {}.getType()),
            createCrawlerUrlDto(aRequestUrl))
        .invoke();
  }
}
