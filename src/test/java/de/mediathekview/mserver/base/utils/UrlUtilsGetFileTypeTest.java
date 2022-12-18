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
public class UrlUtilsGetFileTypeTest {

  private final String inputUrl;
  private final Optional<String> expectedFileType;

  public UrlUtilsGetFileTypeTest(final String aInputUrl, final Optional<String> aExpectedFileType) {
    inputUrl = aInputUrl;
    expectedFileType = aExpectedFileType;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {null, Optional.empty()},
          {"", Optional.empty()},
          {"https://www.testurl.de/my.mp4", Optional.of("mp4")},
          {"http://www.test.de/test.html", Optional.of("html")},
          {"https://test.net/media/37846273_,K,.mp4.csmil/master.m3u8", Optional.of("m3u8")},
          {"https://test.net/media/37846273_,K,.mp4.csmil/manifest.f4m?hdcore", Optional.of("f4m")},
          {"https://teest.net/i/dsa/FS/MT/testfile_,M,.mp4.csmil/master.m3u8", Optional.of("m3u8")},
          {
            "http://br-i.akamaihd.net/i/mir-live/bw1XsLzS/bLQH/bLOliLioMXZhiKT1/uLoXb69zbX06/MUJIuUOVBwQIb71S/bLWCMUJIuUOVBwQIb71S/_2rp9U1S/_-JS/_-Fp_H1S/d6b48cc8-60f3-4625-a56a-fba68c0841c7_,0,A,B,E,C,.mp4.csmil/master.m3u8?__b__=200",
            Optional.of("m3u8")
          }
        });
  }

  @Test
  public void getFileTypeTest() {
    final Optional<String> actual = UrlUtils.getFileType(inputUrl);

    assertThat(actual, equalTo(expectedFileType));
  }
}
