package de.mediathekview.mserver.testhelper;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.junit.Rule;

import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

/** base class of tests with WireMock. */
public abstract class WireMockTestBase {
  public static final String MOCK_URL_BASE = "http://localhost:8589";
  @Rule public WireMockRule wireMockRule = new WireMockRule(8589);

  protected ConcurrentLinkedQueue<CrawlerUrlDTO> createCrawlerUrlDto(final String aRequestUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO(MOCK_URL_BASE + aRequestUrl));
    return input;
  }

  protected void setupSuccessfulJsonResponse(final String aRequestUrl, final String aResponseFile) {
    final String jsonBody = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(
        get(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulJsonPostResponse(
      final String aRequestUrl, final String aResponseFile) {
    final String jsonBody = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(
        post(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulXmlResponse(final String aRequestUrl, final String aResponseFile) {
    final String xmlBody = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(
        get(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/xml")
                    .withStatus(200)
                    .withBody(xmlBody)));
  }

  protected void setupSuccessfulResponse(final String aRequestUrl, final String aResponseFile) {
    final String body = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(
        get(urlEqualTo(aRequestUrl)).willReturn(aResponse().withStatus(200).withBody(body)));
  }

  protected void setupHeadResponse(final String aRequestUrl, final int aHttpCode) {
    wireMockRule.stubFor(
        head(urlEqualTo(aRequestUrl)).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupHeadResponse(final int aHttpCode) {
    wireMockRule.stubFor(head(anyUrl()).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupResponseWithoutBody(final String aRequestUrl, final int aHttpCode) {
    wireMockRule.stubFor(
        get(urlEqualTo(aRequestUrl)).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupHeadRequestForFileSize() {
    wireMockRule.stubFor(
        head(urlMatching(".*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Length", "1")));
  }
}
