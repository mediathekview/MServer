package de.mediathekview.mserver.base.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class UrlUtilsGetUrlParameterValueTest {

  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {null, "test", Optional.empty()},
          {"", "test", Optional.empty()},
          {"https://www.testurl.de/resource?query=3", "query", Optional.of("3")},
          {"https://www.testurl.de/resource?query=result&top=4", "query", Optional.of("result")},
          {"https://www.testurl.de/resource?query=result&top=4", "top", Optional.of("4")},
          {"https://www.testurl.de/resource?query=result&top=4", "notfound", Optional.empty()},
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void getUrlParameterValueTest(
      final String inputUrl,
      final String parameterName,
      final Optional<String> expectedParameterValue) {
    final Optional<String> actual = UrlUtils.getUrlParameterValue(inputUrl, parameterName);

    assertThat(actual, equalTo(expectedParameterValue));
  }
}
