package de.mediathekview.mserver.base.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsGetLastSegmentTest {

  public static Collection<Object[]> data() {
    return Arrays.asList(
            new Object[][] {
                    {null, Optional.empty()},
                    {"", Optional.empty()},
                    {"https://www.testurl.de/my.mp4", Optional.of("my.mp4")},
                    {"http://www.test.de/test.html", Optional.of("test.html")},
                    {"https://test.net/media/37846273_,K,.mp4.csmil", Optional.of("37846273_,K,.mp4.csmil")},
                    {"https://test.net", Optional.empty()}
            });
  }

  @MethodSource("data")
  @ParameterizedTest
  void getLastSegmentTest(final String inputUrl, final Optional<String> expectedResult) {
    final Optional<String> actual = UrlUtils.getLastSegment(inputUrl);

    assertThat(actual, equalTo(expectedResult));
  }
}
