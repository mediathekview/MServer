package de.mediathekview.mserver.testhelper;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.Nullable;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;

/** base class of tests with WireMock. */
public abstract class WireMockTestBaseJUnit5 {
  private final Logger LOG = LoggerFactory.getLogger(WireMockTestBaseJUnit5.class);
  protected WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());
  private boolean wireMockStarted = false;

  @BeforeEach
  public void setUpClass() {
    LOG.info("Setting up WireMock test class");
    startWireMock();
  }

  protected synchronized void startWireMock() {
    if (wireMockStarted) {
      LOG.info("Trying to start already started WireMock");
    } else {
      LOG.info("Starting WireMock");
      wireMockServer.start();
      wireMockStarted = true;
    }
  }

  @AfterEach
  public void tearDownClass() {
    LOG.info("Tear down WireMock test class");
    LOG.info("Stopping WireMock");
    wireMockServer.stop();
    wireMockStarted = false;
  }

  protected String buildWireMockUrl(final String url) {
    if (url != null && url.startsWith("/")) {
      return getWireMockBaseUrlSafe() + url;
    }
    return url;
  }

  protected String fixupAllWireMockUrls(final String text) {
    if (text == null) {
      return null;
    }
    return text.replaceAll("localhost:\\d+", getWireMockHostPort());
  }

  protected String getWireMockBaseUrlSafe() {
    if (!wireMockStarted) {
      startWireMock();
    }
    return wireMockServer.baseUrl();
  }

  protected Queue<CrawlerUrlDTO> createCrawlerUrlDto(final String requestUrl) {
    final Queue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO(getWireMockBaseUrlSafe() + requestUrl));
    return input;
  }

  protected void setupSuccessfulJsonResponse(final String requestUrl, final String aResponseFile) {
    final String jsonBody = FileReader.readFile(aResponseFile, getWireMockHostPort());
    LOG.info("Adding successful JSON response stub for {}", requestUrl);
    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl))
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
      final String requestUrl, final String aResponseFile) {
    setupSuccessfulJsonPostResponse(requestUrl, aResponseFile, null);
  }

  protected void setupSuccessfulJsonPostResponse(
      final String requestUrl, final String aResponseFile, @Nullable final Integer status) {
    final String jsonBody = FileReader.readFile(aResponseFile, getWireMockHostPort());
    LOG.info("Adding successful JSON post response stub for {}", requestUrl);
    wireMockServer.stubFor(
        post(urlEqualTo(requestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(Optional.ofNullable(status).orElse(200))
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulJsonPostResponse(
      final String requestUrl, final String responsefile, final String requestBodyPart, @Nullable final Integer status) {
    final String jsonBody = FileReader.readFile(responsefile);
    wireMockServer.stubFor(
        post(urlEqualTo(requestUrl))
            .withRequestBody(containing(requestBodyPart))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(Optional.ofNullable(status).orElse(200))
                    .withBody(jsonBody)));
  }

  protected void setupSuccessfulXmlResponse(final String requestUrl, final String aResponseFile) {
    final String xmlBody = FileReader.readFile(aResponseFile, getWireMockHostPort());
    LOG.info("Adding successful XML response stub for {}", requestUrl);
    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/xml")
                    .withStatus(200)
                    .withBody(xmlBody)));
  }

  protected void setupSuccessfulResponse(final String requestUrl, final String aResponseFile) {
    final String body = FileReader.readFile(aResponseFile, getWireMockHostPort());
    LOG.info("Adding successful response stub for {}", requestUrl);
    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(200).withBody(body)));
  }

  protected void setupHeadResponse(final String requestUrl, final int aHttpCode) {
    LOG.info("Adding successful HEAD response stub for {}", requestUrl);
    wireMockServer.stubFor(
        head(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupHeadResponse(final int aHttpCode) {
    LOG.info("Adding {} HEAD response stub for any URL.", aHttpCode);
    wireMockServer.stubFor(head(anyUrl()).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupResponseWithoutBody(final String requestUrl, final int aHttpCode) {
    LOG.info("Adding {} stub for {}.", aHttpCode, requestUrl);
    wireMockServer.stubFor(
        get(urlEqualTo(requestUrl)).willReturn(aResponse().withStatus(aHttpCode)));
  }

  protected void setupHeadRequestForFileSize() {
    LOG.info("Adding file size HEAD request stub for any url.");
    wireMockServer.stubFor(
        head(urlMatching(".*"))
            .willReturn(aResponse().withStatus(200).withHeader("Content-Length", "1")));
  }
}
