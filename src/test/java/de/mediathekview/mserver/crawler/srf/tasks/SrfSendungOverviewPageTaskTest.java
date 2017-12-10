package de.mediathekview.mserver.crawler.srf.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.parser.SrfSendungOverviewDTO;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.Optional;
import java.util.Set;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import org.hamcrest.Matchers;
import static org.junit.Assert.assertThat;
import org.junit.Test;

public class SrfSendungOverviewPageTaskTest extends SrfTaskTestBase {
  
  @Test
  public void testOverviewWithSinglePage() {
    CrawlerUrlDTO[] expectedUrls = new CrawlerUrlDTO[] { 
      new CrawlerUrlDTO("https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/69cf918f-185a-4806-92f6-031e7f09844d.json"),
      new CrawlerUrlDTO("https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/4eb1dbdf-dab8-4690-ba93-fdbafebbd5de.json"),
      new CrawlerUrlDTO("https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/af4c9505-c265-49f6-86c8-67fe90dd0a2f.json"),
      new CrawlerUrlDTO("https://il.srgssr.ch/integrationlayer/2.0/srf/mediaComposition/video/22b9dd2c-d1fd-463b-91de-d804eda74889.json")
    };

    String requestUrl = "/play/v2/tv/show/c5a89422-4580-0001-4f24-1889dc30d730/latestEpisodes?numberOfEpisodes=10&tillMonth=12-2017&layout=json";
    String jsonBody = FileReader.readFile("/srf/srf_sendung_overview_page_last.json");
    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));

    final Set<SrfSendungOverviewDTO> actual = executeTask(requestUrl);
    
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(1));
    
    SrfSendungOverviewDTO actualDto = actual.toArray(new SrfSendungOverviewDTO[0])[0];
    assertThat(actualDto, notNullValue());
    assertThat(actualDto.getNextPageId(), equalTo(Optional.empty()));
    assertThat(actualDto.getUrls(), Matchers.containsInAnyOrder(expectedUrls));
  }  

  @Test
  public void testOverviewPageNotFound() {
    String requestUrl = "/play/v2/tv/show/c5a89422-4580-0001-4f24-1889dc30d730/latestEpisodes?numberOfEpisodes=10&tillMonth=12-2017&layout=json";
    
    wireMockRule.stubFor(get(urlEqualTo(requestUrl))
      .willReturn(aResponse()
        .withStatus(404)
        .withBody("Not Found")));
    
    final Set<SrfSendungOverviewDTO> actual = executeTask(requestUrl);
    assertThat(actual, notNullValue());
    assertThat(actual.size(), equalTo(0));
  }  
  
  private Set<SrfSendungOverviewDTO> executeTask(String aRequestUrl) {
    return new SrfSendungOverviewPageTask(createCrawler(), createCrawlerUrlDto(aRequestUrl)).invoke();    
  }
}
