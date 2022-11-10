package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsRemoveParametersTest {
  private final String inputUrl;
  private final String expectedUrl;

  public UrlUtilsRemoveParametersTest(final String aInputUrl, final String aExpectedUrl) {
    inputUrl = aInputUrl;
    expectedUrl = aExpectedUrl;
  }

  @Parameterized.Parameters
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

  @Test
  public void removeParameters() {
    final String actual = UrlUtils.removeParameters(inputUrl);

    assertThat(actual, equalTo(expectedUrl));
  }
}
