package de.mediathekview.mserver.base.utils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class UrlUtilsTestGetFileType {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { null, Optional.empty() },
      { "", Optional.empty() },
      { "https://www.testurl.de/my.mp4", Optional.of("mp4") },
      { "http://www.test.de/test.html", Optional.of("html") },
      { "https://test.net/media/37846273_,K,.mp4.csmil/master.m3u8", Optional.of("m3u8") },
      { "https://test.net/media/37846273_,K,.mp4.csmil/manifest.f4m?hdcore", Optional.of("f4m?hdcore") }
    });
  }
  
  private final String inputUrl;
  private final Optional<String> expectedFileType;
  
  public UrlUtilsTestGetFileType(String aInputUrl, Optional<String> aExpectedFileType) {
    inputUrl = aInputUrl;
    expectedFileType = aExpectedFileType;
  }
  
  @Test
  public void getFileTypeTest() {
    Optional<String> actual = UrlUtils.getFileType(inputUrl);
    
    assertThat(actual, equalTo(expectedFileType));
  }
}
