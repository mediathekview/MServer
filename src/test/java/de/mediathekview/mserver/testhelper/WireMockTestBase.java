package de.mediathekview.mserver.testhelper;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.head;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlMatching;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/** base class of tests with WireMock. */
public abstract class WireMockTestBase {

  protected static WireMockServer wireMockServer = new WireMockServer(options().port(8589));

  @BeforeClass
  public static void setUpClass() {
    wireMockServer.start();
  }

  @AfterClass
  public static void tearDownClass() {
    wireMockServer.stop();
  }

  protected ConcurrentLinkedQueue<CrawlerUrlDTO> createCrawlerUrlDto(final String aRequestUrl) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO(wireMockServer.baseUrl() + aRequestUrl));
    return input;
  }

  protected void setupSuccessfulJsonResponse(final String aRequestUrl, final String aResponseFile) {
    final String jsonBody = FileReader.readFile(aResponseFile);
    wireMockServer.stubFor(
        get(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulJsonPostResponse(
      final String aRequestUrl, final String aResponseFile) {
    setupSuccessfulJsonPostResponse(aRequestUrl, aResponseFile, Optional.empty());
  }

  protected void setupSuccessfulJsonPostResponse(
      final String aRequestUrl, final String aResponseFile, final Optional<Integer> status) {
    final String jsonBody = FileReader.readFile(aResponseFile);
    wireMockServer.stubFor(
        post(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(status.orElse(200))
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulXmlResponse(final String aRequestUrl, final String aResponseFile) {
    final String xmlBody = FileReader.readFile(aResponseFile);
    wireMockServer.stubFor(
        get(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/xml")
                    .withStatus(200)
                    .withBody(xmlBody)));
  }

  protected void setupSuccessfulResponse(final String aRequestUrl, final String aResponseFile) {
    final String body = FileReader.readFile(aResponseFile);
    wireMockServer.stubFor(
        get(urlEqualTo(aRequestUrl)).willReturn(aResponse().withStatus(200).withBody(body)));
  }

  protected void setupHeadResponse(final String aRequestUrl, final int aHttpCode) {
    wireMockServer.stubFor(
        head(urlEqualTo(aRequestUrl)).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupHeadResponse(final int aHttpCode) {
    wireMockServer.stubFor(head(anyUrl()).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupResponseWithoutBody(final String aRequestUrl, final int aHttpCode) {
    wireMockServer.stubFor(
        get(urlEqualTo(aRequestUrl)).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupHeadRequestForFileSize() {
    wireMockServer.stubFor(
        head(urlMatching(".*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Length", "1")));
  }
}
