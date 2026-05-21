package de.mediathekview.mserver.base.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsGetBaseUrlTest {
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

  @MethodSource("data")
  @ParameterizedTest
  void getBaseUrlTest(final String inputUrl, final String expectedBaseUrl) {
    final String actual = UrlUtils.getBaseUrl(inputUrl);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
