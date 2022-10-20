package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import org.junit.Test;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ArteSubcategoryVideosTaskTest extends ArteTaskTestBase {
  @Test
  public void testOverviewWithSinglePage() {
    final String requestUrl =
        "/guide/api/emac/v3/de/web/zones/videos_subcategory/?id=ART&limit=100&page=1";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_films_page_last.json");

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, "ART", ArteLanguage.DE);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(5));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(5);

    final String requestUrl =
        "/api/rproxy/emac/v3/de/web/data/MOST_RECENT_SUBCATEGORY/?subCategoryCode=ART&page=1&limit=100";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_films_page1.json");
    setupSuccessfulJsonResponse(
        "/api/rproxy/emac/v3/de/web/data/MOST_RECENT_SUBCATEGORY/?subCategoryCode=ART&page=2&limit=100",
        "/arte/arte_subcategory_films_page_last.json");

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, "ART", ArteLanguage.DE);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(10));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.ARTE_DE).setMaximumSubpages(1);

    final String requestUrl =
        "/api/rproxy/emac/v3/de/web/data/MOST_RECENT_SUBCATEGORY/?subCategoryCode=ART&page=1&limit=100";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_films_page1.json");

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, "ART", ArteLanguage.DE);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(5));
  }

  @Test
  public void testOverviewPageNotFound() {
    final String requestUrl =
        "/guide/api/emac/v3/de/web/data/MOST_RECENT_SUBCATEGORY/?subCategoryCode=ART&page=1&limit=100";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, "ART", ArteLanguage.DE);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<ArteFilmUrlDto> executeTask(
      final String aRequestUrl, final String aTopic, final ArteLanguage language) {
    return new ArteSubcategoryVideosTask(
            createCrawler(),
            createTopicUrlDto(aRequestUrl, aTopic),
            getWireMockBaseUrlSafe(),
            language)
        .invoke();
  }

  private Queue<TopicUrlDTO> createTopicUrlDto(final String aRequestUrl, final String aTopic) {
    final Queue<TopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new TopicUrlDTO(aTopic, getWireMockBaseUrlSafe() + aRequestUrl));
    return input;
  }
}
