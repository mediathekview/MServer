package de.mediathekview.mserver.base.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsRemoveParametersTest {

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {null, null},
          {"", ""},
          {"https://www.testurl.de/resource?query=3", "https://www.testurl.de/resource"},
          {"https://www.testurl.de/resource?query=result&top=4", "https://www.testurl.de/resource"},
          {"https://www.testurl.de/resource", "https://www.testurl.de/resource"}
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void removeParameters(final String inputUrl, final String expectedUrl) {
    final String actual = UrlUtils.removeParameters(inputUrl);

    assertThat(actual, equalTo(expectedUrl));
  }
}
