package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import org.junit.Test;

import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class ArteSubcategoriesTaskTest extends ArteTaskTestBase {

  @Test
  public void testOverviewWithSinglePage() {
    final String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_page_last.json");

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesLargerThanSubpageCount() {

    rootConfig.getConfig().setMaximumSubpages(5);

    final String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_page1.json");
    setupSuccessfulJsonResponse(
        "/api/opa/v3/subcategories?language=de&limit=5&page=2",
        "/arte/arte_subcategory_page2.json");
    setupSuccessfulJsonResponse(
        "/api/opa/v3/subcategories?language=de&limit=5&page=3",
        "/arte/arte_subcategory_page_last.json");

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(11));
  }

  @Test
  public void testOverviewWithMultiplePagesLimitSubpagesSmallerThanSubpageCount() {
    rootConfig.getSenderConfig(Sender.ARTE_DE).setMaximumSubpages(2);

    final String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";
    setupSuccessfulJsonResponse(requestUrl, "/arte/arte_subcategory_page1.json");
    setupSuccessfulJsonResponse(
        "/api/opa/v3/subcategories?language=de&limit=5&page=2",
        "/arte/arte_subcategory_page2.json");

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(10));
  }

  @Test
  public void testOverviewPageNotFound() {
    final String requestUrl = "/api/opa/v3/subcategories?language=de&limit=5";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<TopicUrlDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<TopicUrlDTO> executeTask(final String aRequestUrl) {
    return new ArteSubcategoriesTask(createCrawler(), createCrawlerUrlDto(aRequestUrl)).invoke();
  }
}
