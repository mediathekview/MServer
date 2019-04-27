package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.hamcrest.Matchers;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public class SrfSendungOverviewPageTaskTest extends SrfTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() {
      CrawlerUrlDTO[] expectedUrls =
              new CrawlerUrlDTO[]{
                      new CrawlerUrlDTO(
                              "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/69cf918f-185a-4806-92f6-031e7f09844d.json"),
                      new CrawlerUrlDTO(
                              "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/4eb1dbdf-dab8-4690-ba93-fdbafebbd5de.json"),
                      new CrawlerUrlDTO(
                              "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/af4c9505-c265-49f6-86c8-67fe90dd0a2f.json"),
                      new CrawlerUrlDTO(
                              "https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json")
              };

      String requestUrl =
              "/play/v2/tv/show/c5a89422-4580-0001-4f24-1889dc30d730/latestEpisodes?numberOfEpisodes=10&tillMonth=12-2017&layout=json";
    setupSuccessfulJsonResponse(requestUrl, "/srf/srf_sendung_overview_page_last.json");

    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedUrls));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(5);

      String requestUrl =
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?numberOfEpisodes=10&tillMonth=01-2018&layout=json";
    setupSuccessfulJsonResponse(requestUrl, "/srf/srf_sendung_overview_multiple_page1.json");
      setupSuccessfulJsonResponse(
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?nextPageHash=09e12b6f403c2da8bfde15a1c99070d4f1c58eef3c29b0ea2f598fc7a2dcbbae84b119d4b008f929b9a8062b1cedecbc0555797c10cff0a3f497f348faab3d1c4b52af6a2583a9ce80db3d0defd5467424f1db5b89b2c9f1cfdc0b0b5c2bded71fe192eb41d68d868a71cf7e927ac094ce05fcba9ee06335&tillMonth=01-2018",
              "/srf/srf_sendung_overview_multiple_page2.json");
      setupSuccessfulJsonResponse(
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?nextPageHash=09e12b6f403c2da8bfde15a1c99070d4f1c58eef3c29b0ea2f598fc7a2dcbbae84b119d4b008f929b9a8062b1cedecbc0555797c10cff0a3f497f348faab3d1c4b52af6a2583a9ce80db3d0defd5467424f1db5b89b2c9f1cfdc0b0b5c2bded787c5e3477ef68dbdffdb28ad6425e9a7101f9c5bd533c2ad&tillMonth=01-2018",
              "/srf/srf_sendung_overview_multiple_page3.json");
      setupSuccessfulJsonResponse(
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?nextPageHash=09e12b6f403c2da8bfde15a1c99070d4f1c58eef3c29b0ea2f598fc7a2dcbbae84b119d4b008f929b9a8062b1cedecbc0555797c10cff0a3f497f348faab3d1c4b52af6a2583a9ce80db3d0defd5467424f1db5b89b2c9f1cfdc0b0b5c2bded7ceaa513e3b2675dfe2658f87f8c511ae540c849dab77c523&tillMonth=01-2018",
              "/srf/srf_sendung_overview_multiple_page4.json");

    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(40));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
      String requestUrl =
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?numberOfEpisodes=10&tillMonth=01-2018&layout=json";
    setupSuccessfulJsonResponse(requestUrl, "/srf/srf_sendung_overview_multiple_page1.json");
      setupSuccessfulJsonResponse(
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?nextPageHash=09e12b6f403c2da8bfde15a1c99070d4f1c58eef3c29b0ea2f598fc7a2dcbbae84b119d4b008f929b9a8062b1cedecbc0555797c10cff0a3f497f348faab3d1c4b52af6a2583a9ce80db3d0defd5467424f1db5b89b2c9f1cfdc0b0b5c2bded71fe192eb41d68d868a71cf7e927ac094ce05fcba9ee06335&tillMonth=01-2018",
              "/srf/srf_sendung_overview_multiple_page2.json");
      setupSuccessfulJsonResponse(
              "/play/v2/tv/show/c3f6b6b4-0770-0001-42bf-1f101bb44800/latestEpisodes?nextPageHash=09e12b6f403c2da8bfde15a1c99070d4f1c58eef3c29b0ea2f598fc7a2dcbbae84b119d4b008f929b9a8062b1cedecbc0555797c10cff0a3f497f348faab3d1c4b52af6a2583a9ce80db3d0defd5467424f1db5b89b2c9f1cfdc0b0b5c2bded787c5e3477ef68dbdffdb28ad6425e9a7101f9c5bd533c2ad&tillMonth=01-2018",
              "/srf/srf_sendung_overview_multiple_page3.json");

    rootConfig.getConfig().setMaximumSubpages(3);
    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);

      assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(30));
  }

    @Test
  public void testOverviewPageNotFound() {
        String requestUrl =
                "/play/v2/tv/show/c5a89422-4580-0001-4f24-1889dc30d730/latestEpisodes?numberOfEpisodes=10&tillMonth=12-2017&layout=json";

        wireMockRule.stubFor(
                get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
    }

  private Set<CrawlerUrlDTO> executeTask(String aRequestUrl) {
      return new SrfSendungOverviewPageTask(createCrawler(), createCrawlerUrlDto(aRequestUrl))
              .invoke();
  }
}
