package de.mediathekview.mserver.crawler.arte;

import com.google.common.util.concurrent.RateLimiter;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.io.IOException;
import java.util.concurrent.CancellationException;
import org.junit.jupiter.api.Test;
import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class ArteHttpClientTest extends WireMockTestBase {
  @Test
  void retriesRateLimitsWithoutObsoleteAuthorization() throws IOException {
    wireMockServer.stubFor(get(urlEqualTo("/rate")).inScenario("rate").whenScenarioStateIs("Started")
        .willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0"))
        .willSetStateTo("ok"));
    wireMockServer.stubFor(get(urlEqualTo("/rate")).inScenario("rate").whenScenarioStateIs("ok")
        .willReturn(aResponse().withBody("success")));
    try (var client = new ArteHttpClient(5, RateLimiter.create(1000))) {
      assertEquals("success", client.get(getWireMockBaseUrlSafe() + "/rate"));
    }
    wireMockServer.verify(2, getRequestedFor(urlEqualTo("/rate")).withoutHeader("Authorization"));
  }

  @Test
  void skipsExpiredPlayersAndDoesNotRetryForbidden() throws IOException {
    wireMockServer.stubFor(get(urlEqualTo("/expired")).willReturn(aResponse().withStatus(410)));
    wireMockServer.stubFor(get(urlEqualTo("/forbidden")).willReturn(aResponse().withStatus(403)));
    try (var client = new ArteHttpClient(5, RateLimiter.create(1000))) {
      assertEquals("", client.get(getWireMockBaseUrlSafe() + "/expired"));
      assertThrows(IOException.class, () -> client.get(getWireMockBaseUrlSafe() + "/forbidden"));
    }
    wireMockServer.verify(1, getRequestedFor(urlEqualTo("/forbidden")));
  }
  @Test
  void aCallTimeoutRemainsAnIoFailureAndDoesNotCancelTheClient() throws IOException {
    wireMockServer.stubFor(get(urlEqualTo("/slow"))
        .willReturn(aResponse().withFixedDelay(1500).withBody("late")));
    wireMockServer.stubFor(get(urlEqualTo("/healthy")).willReturn(aResponse().withBody("healthy")));
    try (var client = new ArteHttpClient(1, RateLimiter.create(1000))) {
      assertThrows(IOException.class, () -> client.get(getWireMockBaseUrlSafe() + "/slow"));
      assertFalse(Thread.currentThread().isInterrupted());
      assertEquals("healthy", client.get(getWireMockBaseUrlSafe() + "/healthy"));
    }
  }

  @Test
  void closedTransportRejectsRequestsAsCancellation() {
    var client = new ArteHttpClient(5, RateLimiter.create(1000));
    client.close();
    client.close();
    assertThrows(CancellationException.class, () -> client.get(getWireMockBaseUrlSafe() + "/closed"));
    wireMockServer.verify(0, getRequestedFor(urlEqualTo("/closed")));
  }

}
