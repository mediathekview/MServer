package de.mediathekview.mserver.crawler.arte.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import de.mediathekview.mserver.crawler.arte.ArteFilmUrlDto;
import de.mediathekview.mserver.crawler.arte.ArteLanguage;
import java.util.Set;
import org.junit.Test;

public class ArteSubcategoryVideosTaskTest extends ArteTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() {
    String requestUrl = "/guide/api/api/zones/de/videos_subcategory/?id=ART&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_films_page_last.json");

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, ArteLanguage.DE);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(5));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(5);

    String requestUrl = "/guide/api/api/zones/de/videos_subcategory/?id=ART&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_films_page1.json");
    setupSuccessfulJsonResponse("/api/emac/v3/de/web/zones/videos_subcategory?id=ART&page=2&limit=5",
        "/arte/arte_subcategory_films_page_last.json");

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, ArteLanguage.DE);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(10));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(1);

    String requestUrl = "/guide/api/api/zones/de/videos_subcategory/?id=ART&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_films_page1.json");

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, ArteLanguage.DE);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(5));
  }

  @Test
  public void testOverviewPageNotFound() {
    String requestUrl = "/guide/api/api/zones/de/videos_subcategory/?id=ART&limit=5";

    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(404)
            .withBody("Not Found")));

    final Set<ArteFilmUrlDto> actual = executeTask(requestUrl, ArteLanguage.DE);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<ArteFilmUrlDto> executeTask(String aRequestUrl, ArteLanguage language) {
    return new ArteSubcategoryVideosTask(createCrawler(), createCrawlerUrlDto(aRequestUrl), language).invoke();
  }

}