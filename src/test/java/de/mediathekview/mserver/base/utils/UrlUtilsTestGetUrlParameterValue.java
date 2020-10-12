package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsTestGetUrlParameterValue {
  private final String inputUrl;
  private final String parameterName;
  private final Optional<String> expectedParameterValue;

  public UrlUtilsTestGetUrlParameterValue(
          final String aInputUrl, final String aParameterName, final Optional<String> aExpectedParameterValue) {
    inputUrl = aInputUrl;
    parameterName = aParameterName;
    expectedParameterValue = aExpectedParameterValue;
  }

  @Parameterized.Parameters
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

  @Test
  public void getUrlParameterValueTest() throws UrlParseException {
    final Optional<String> actual = UrlUtils.getUrlParameterValue(inputUrl, parameterName);

    assertThat(actual, equalTo(expectedParameterValue));
  }
}
