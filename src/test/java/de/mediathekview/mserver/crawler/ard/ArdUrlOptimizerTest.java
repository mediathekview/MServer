package de.mediathekview.mserver.crawler.ard;

import static org.assertj.core.api.Assertions.assertThat;

import de.mediathekview.mserver.crawler.ard.tasks.ArdTaskTestBase;
import org.junit.jupiter.api.Test;

class ArdUrlOptimizerTest extends ArdTaskTestBase {

  @Test
  void optimizedHdUrlNotAvailable() {
    runTest("/br_film_X.mp4", "/br_film_HD.mp4", 404, "/br_film_X.mp4");
  }

  @Test
  void optimizedHdUrlBr() {
    runTest("/br_film_X.mp4", "/br_film_HD.mp4", 200, "/br_film_HD.mp4");
  }

  @Test
  void optimizedHdUrlWdr() {
    // original host wdrmedien-a.akamaihd.net used in path because wdrmedien is used to filter wdr urls
    runTest(
        "/wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk0/262/2625725/2625725_54085881.mp4",
        "/wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk0/262/2625725/2625725_54085877.mp4",
        200,
        "/wdrmedien-a.akamaihd.net/medp/ondemand/de/fsk0/262/2625725/2625725_54085877.mp4");
  }

  private void runTest(String url, String optimizedUrl, int headResponseCode, String expectedUrl) {
    final String baseUrl = getWireMockBaseUrlSafe();
    setupHeadResponse(optimizedUrl, headResponseCode);

    ArdUrlOptimizer optimizer = new ArdUrlOptimizer(createCrawler());
    final String actualUrl = optimizer.optimizeHdUrl(baseUrl + url);
    assertThat(actualUrl).isEqualTo(baseUrl + expectedUrl);
  }
}
