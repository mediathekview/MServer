package de.mediathekview.mserver.base.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsGetProtocolTest {
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {null, Optional.empty()},
          {"", Optional.empty()},
          {"https://www.testurl.de/resource?query=3", Optional.of("https:")},
          {"www.urlohneschema.de/child", Optional.empty()},
          {"http://www.test.de", Optional.of("http:")},
          {"rtmp://www.test.de", Optional.of("rtmp:")},
        });
  }

  @MethodSource("data")
  @ParameterizedTest
  void getBaseUrlTest(final String inputUrl, final Optional<String> expectedProtocol) {
    final Optional<String> actual = UrlUtils.getProtocol(inputUrl);

    assertThat(actual, equalTo(expectedProtocol));
  }
}
