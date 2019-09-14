package de.mediathekview.mserver.base.utils;

import de.mediathekview.mserver.testhelper.WireMockTestBase;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsTestExistsUrl extends WireMockTestBase {

  @Test
  public void existsUrlTestNull() {
    final String url = null;

    boolean actual = UrlUtils.existsUrl(url);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void existsUrlTestOk() {
    final String url = "/my/url/exists";
    setupHeadResponse(url, 200);

    boolean actual = UrlUtils.existsUrl(WireMockTestBase.MOCK_URL_BASE + url);

    assertThat(actual, equalTo(true));
  }

  @Test
  public void existsUrlTestNotFound() {
    final String url = "/my/url/exists";
    setupHeadResponse(url, 404);

    boolean actual = UrlUtils.existsUrl(WireMockTestBase.MOCK_URL_BASE + url);

    assertThat(actual, equalTo(false));
  }

  @Test
  public void existsUrlTestInvalidUrl() {
    final String url = ":/:notvalid";

    boolean actual = UrlUtils.existsUrl(WireMockTestBase.MOCK_URL_BASE + url);

    assertThat(actual, equalTo(false));
  }
}
