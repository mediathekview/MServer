package de.mediathekview.mserver.testhelper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.Rule;

/**
 * base class of tests with WireMock
 */
public abstract class WireMockTestBase {
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8589);

  protected ConcurrentLinkedQueue<CrawlerUrlDTO> createCrawlerUrlDto(String aRequestUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO("http://localhost:8589" + aRequestUrl));
    return input;
  }
  
  protected void setupSuccessfulJsonResponse(String aRequestUrl, String aResponseFile) {
    String jsonBody = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(get(urlEqualTo(aRequestUrl))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));
  }  
  
  protected void setupSuccessfulResponse(String aRequestUrl, String aResponseFile) {
    String body = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(get(urlEqualTo(aRequestUrl))
            .willReturn(aResponse()
                    .withStatus(200)
                    .withBody(body)));
  }
  
  protected void setupResponseWithoutBody(String aRequestUrl, int aHttpCode) {
    wireMockRule.stubFor(get(urlEqualTo(aRequestUrl))
            .willReturn(aResponse()
                    .withStatus(aHttpCode)));
  }
}
