package de.mediathekview.mserver.crawler.swr;

import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class SwrUrlOptimizerTest extends WireMockTestBase {

  @Test
  public void optimizeHdUrlTestFullHdExists() {
    final String url = WireMockTestBase.MOCK_URL_BASE + "/845421.xl.mp4";
    final String expectedUrl = WireMockTestBase.MOCK_URL_BASE + "/845421.xxl.mp4";
    setupHeadResponse("/845421.xxl.mp4", 200);

    SwrUrlOptimizer target = new SwrUrlOptimizer();
    String actualUrl = target.optimizeHdUrl(url);

    assertThat(actualUrl, equalTo(expectedUrl));
  }

  @Test
  public void optimizeHdUrlTestFullHdDoesNotExists() {
    final String url = WireMockTestBase.MOCK_URL_BASE + "/845421.xl.mp4";
    setupHeadResponse("/845421.xxl.mp4", 404);

    SwrUrlOptimizer target = new SwrUrlOptimizer();
    String actualUrl = target.optimizeHdUrl(url);

    assertThat(actualUrl, equalTo(url));
  }

  @Test
  public void optimizeHdUrlTestNoUrlToOptimize() {
    final String url = WireMockTestBase.MOCK_URL_BASE + "/78946584.l.mp4";

    SwrUrlOptimizer target = new SwrUrlOptimizer();
    String actualUrl = target.optimizeHdUrl(url);

    assertThat(actualUrl, equalTo(url));
  }
}
