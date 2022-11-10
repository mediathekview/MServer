package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsGetBaseUrlTest {
  private final String inputUrl;
  private final String expectedBaseUrl;

  public UrlUtilsGetBaseUrlTest(final String aInputUrl, final String aExpectedBaseUrl) {
    inputUrl = aInputUrl;
    expectedBaseUrl = aExpectedBaseUrl;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {null, null},
          {"", ""},
          {"https://www.testurl.de/resource?query=3", "https://www.testurl.de"},
          {"www.urlohneschema.de/child", "www.urlohneschema.de"},
          {"http://www.test.de", "http://www.test.de"}
        });
  }

  @Test
  public void getBaseUrlTest() {
    final String actual = UrlUtils.getBaseUrl(inputUrl);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
