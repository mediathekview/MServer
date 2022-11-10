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
public class UrlUtilsGetFileNameTest {

  private final String inputUrl;
  private final Optional<String> expectedFileType;

  public UrlUtilsGetFileNameTest(final String aInputUrl, final Optional<String> aExpectedFileType) {
    inputUrl = aInputUrl;
    expectedFileType = aExpectedFileType;
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(
        new Object[][] {
          {null, Optional.empty()},
          {"", Optional.empty()},
          {"https://www.testurl.de/my.mp4", Optional.of("my.mp4")},
          {"http://www.test.de/test.html", Optional.of("test.html")},
          {"https://test.net/media/37846273_,K,.mp4.csmil/master.m3u8", Optional.of("master.m3u8")},
          {
            "https://test.net/media/37846273_,K,.mp4.csmil/manifest.f4m",
            Optional.of("manifest.f4m")
          },
          {"https://test.net/media/folder/nofile", Optional.empty()}
        });
  }

  @Test
  public void getFileNameTest() {
    final Optional<String> actual = UrlUtils.getFileName(inputUrl);

    assertThat(actual, equalTo(expectedFileType));
  }
}
