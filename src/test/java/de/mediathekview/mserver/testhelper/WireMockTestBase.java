package de.mediathekview.mserver.testhelper;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jetbrains.annotations.NotNull;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/** base class of tests with WireMock. */
public abstract class WireMockTestBase {

  protected static WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
  private static boolean wireMockStarted = false;

  @BeforeClass
  public static void setUpClass() {
    startWireMock();
  }

  private static void startWireMock() {
    wireMockServer.start();
    wireMockStarted = true;
  }

  @AfterClass
  public static void tearDownClass() {
    wireMockServer.stop();
    wireMockStarted = false;
  }

  protected static String getWireMockBaseUrlSafe() {
    if (!wireMockStarted) {
      startWireMock();
    }
    return wireMockServer.baseUrl();
  }

  protected Queue<CrawlerUrlDTO> createCrawlerUrlDto(final String aRequestUrl) {
    final Queue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + aRequestUrl));
    return input;
  }

  protected void setupSuccessfulJsonResponse(final String aRequestUrl, final String aResponseFile) {
    final String jsonBody = FileReader.readFile(aResponseFile, getWireMockHostPort());
    wireMockServer.stubFor(
        get(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));
  }

  @NotNull
  private String getWireMockHostPort() {
    return "localhost:" + wireMockServer.port();
  }

  protected void setupSuccessfulJsonPostResponse(
      final String aRequestUrl, final String aResponseFile) {
    setupSuccessfulJsonPostResponse(aRequestUrl, aResponseFile, null);
  }

  protected void setupSuccessfulJsonPostResponse(
      final String aRequestUrl, final String aResponseFile, @Nullable final Integer status) {
    final String jsonBody = FileReader.readFile(aResponseFile, getWireMockHostPort());
    wireMockServer.stubFor(
        post(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(Optional.ofNullable(status).orElse(200))
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulXmlResponse(final String aRequestUrl, final String aResponseFile) {
    final String xmlBody = FileReader.readFile(aResponseFile, getWireMockHostPort());
    wireMockServer.stubFor(
        get(urlEqualTo(aRequestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/xml")
                    .withStatus(200)
                    .withBody(xmlBody)));
  }

  protected void setupSuccessfulResponse(final String aRequestUrl, final String aResponseFile) {
    final String body = FileReader.readFile(aResponseFile, getWireMockHostPort());
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
