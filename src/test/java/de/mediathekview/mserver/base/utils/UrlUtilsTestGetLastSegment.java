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
public class UrlUtilsTestGetLastSegment {

  private final String inputUrl;
  private final Optional<String> expectedResult;

  public UrlUtilsTestGetLastSegment(final String inputUrl, final Optional<String> expectedResult) {
    this.inputUrl = inputUrl;
    this.expectedResult = expectedResult;
  }

  @Parameterized.Parameters
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

  @Test
  public void getLastSegmentTest() {
    final Optional<String> actual = UrlUtils.getLastSegment(inputUrl);

    assertThat(actual, equalTo(expectedResult));
  }
}
