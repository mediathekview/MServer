package de.mediathekview.mserver.base.utils;

import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsTestExistsUrl extends WireMockTestBase {

  @Test
  public void existsUrlTestNull() {
    final String url = null;

    final boolean actual = UrlUtils.existsUrl(url);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void existsUrlTestOk() {
    final String url = "/my/url/exists";
    setupHeadResponse(url, 200);

    final boolean actual = UrlUtils.existsUrl(getWireMockBaseUrlSafe() + url);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void existsUrlTestNotFound() {
    final String url = "/my/url/exists";
    setupHeadResponse(url, 404);

    final boolean actual = UrlUtils.existsUrl(getWireMockBaseUrlSafe() + url);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void existsUrlTestInvalidUrl() {
    final String url = ":/:notvalid";

    final boolean actual = UrlUtils.existsUrl(getWireMockBaseUrlSafe() + url);

    assertThat(actual, equalTo(false));
  }
}
