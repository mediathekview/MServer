package de.mediathekview.mserver.crawler.arte.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import java.util.Set;
import org.junit.Test;

public class ArteSubcategoriesTaskTest extends ArteTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() {
    String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_page_last.json");

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(5);

    String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_page1.json");
    setupSuccessfulJsonResponse("/api/opa/v3/subcategories?language=de&limit=5&page=2", "/arte/arte_subcategory_page2.json");
    setupSuccessfulJsonResponse("/api/opa/v3/subcategories?language=de&limit=5&page=3", "/arte/arte_subcategory_page_last.json");

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(11));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(2);

    String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_page1.json");
    setupSuccessfulJsonResponse("/api/opa/v3/subcategories?language=de&limit=5&page=2", "/arte/arte_subcategory_page2.json");
    setupSuccessfulJsonResponse("/api/opa/v3/subcategories?language=de&limit=5&page=3", "/arte/arte_subcategory_page_last.json");

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(10));
  }

  @Test
  public void testOverviewPageNotFound() {
    String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";

    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
        .willReturn(aResponse()
            .withStatus(404)
            .withBody("Not Found")));

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<TopicUrlDTO> executeTask(String aRequestUrl) {
    return new ArteSubcategoriesTask(createCrawler(), createCrawlerUrlDto(aRequestUrl)).invoke();
  }
}