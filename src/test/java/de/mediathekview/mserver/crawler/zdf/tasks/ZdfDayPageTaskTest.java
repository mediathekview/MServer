package de.mediathekview.mserver.crawler.zdf.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.zdf.ZdfConstants;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import org.hamcrest.Matchers;
import org.junit.Test;

public class ZdfDayPageTaskTest extends ZdfTaskTestBase {

  @Test
  public void testWithSinglePage() {

    final CrawlerUrlDTO[] expectedEntries =
        new CrawlerUrlDTO[] {
          new CrawlerUrlDTO("https://api.zdf.de/content/documents/olympia-im-technikwahn-100.json"),
          new CrawlerUrlDTO("https://api.zdf.de/content/documents/gestrandet-102.json"),
          new CrawlerUrlDTO("https://api.zdf.de/content/documents/im-dialog-vom-23022018-100.json"),
          new CrawlerUrlDTO(
              "https://api.zdf.de/content/documents/augstein--blome-vom-23022018-100.json"),
          new CrawlerUrlDTO(
              "https://api.zdf.de/content/documents/menschen---das-magazin-vom-24-februar-2018-100.json"),
          new CrawlerUrlDTO("https://api.zdf.de/content/documents/die-orakel-krake-100.json"),
          new CrawlerUrlDTO(
              "https://api.zdf.de/content/documents/siegerehrung-maenner-staffel-100.json"),
          new CrawlerUrlDTO(
              "https://api.zdf.de/content/documents/siegerehrung-vom-parallelslalom-der-frauen-100.json")
        };

    final String requestUrl = "/test/url";
    setupSuccessfulJsonResponse(requestUrl, "/zdf/zdf_day_page_single.json");

    final Collection<CrawlerUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual, Matchers.containsInAnyOrder(expectedEntries));
  }

  @Test
  public void testWithMultiplePages() {

    final String requestUrl =
        "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=2018-02-24T12:00:00.000%2B01:00&to=2018-02-24T14:00:00.878%2B01:00&sortBy=date&page=1";
    setupSuccessfulJsonResponse(requestUrl, "/zdf/zdf_day_page_multiple1.json");
    setupSuccessfulJsonResponse(
        "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=2018-02-24T12%3A00%3A00.000%2B01%3A00&sortBy=date&to=2018-02-24T18%3A00%3A00.878%2B01%3A00&page=2",
        "/zdf/zdf_day_page_multiple2.json");
    setupSuccessfulJsonResponse(
        "/search/documents?hasVideo=true&q=*&types=page-video&sortOrder=desc&from=2018-02-24T12%3A00%3A00.000%2B01%3A00&sortBy=date&to=2018-02-24T18%3A00%3A00.878%2B01%3A00&page=3",
        "/zdf/zdf_day_page_multiple3.json");

    final Collection<CrawlerUrlDTO> actual = executeTask(requestUrl);

    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(35));
  }

  @Test
  public void testOverviewPageNotFound() {
    final String requestUrl = "/url/notfound";

    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(404).withBody("Not Found")));

    final Set<CrawlerUrlDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }

  private Set<CrawlerUrlDTO> executeTask(final String aRequestUrl) {
    return new ZdfDayPageTask(
            createCrawler(),
            ZdfConstants.URL_API_BASE,
            createCrawlerUrlDto(aRequestUrl),
            Optional.empty())
        .invoke();
  }
}
