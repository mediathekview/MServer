package de.mediathekview.mserver.base.utils;

import java.util.Arrays;
import java.util.Collection;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class UrlUtilsTestGetBaseUrl {
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] { 
      { null, null },
      { "", "" },
      { "https://www.testurl.de/resource?query=3", "https://www.testurl.de" },
      { "www.urlohneschema.de/child", "www.urlohneschema.de" },
      { "http://www.test.de", "http://www.test.de" }
    });
  }
  
  private final String inputUrl;
  private final String expectedBaseUrl;
  
  public UrlUtilsTestGetBaseUrl(String aInputUrl, String aExpectedBaseUrl) {
    inputUrl = aInputUrl;
    expectedBaseUrl = aExpectedBaseUrl;
  }
  
  @Test
  public void getBaseUrlTest() {
    String actual = UrlUtils.getBaseUrl(inputUrl);
    
    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
