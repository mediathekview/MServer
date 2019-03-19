package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsTestGetBaseUrl {
    private final String inputUrl;
    private final String expectedBaseUrl;

    public UrlUtilsTestGetBaseUrl(String aInputUrl, String aExpectedBaseUrl) {
        inputUrl = aInputUrl;
        expectedBaseUrl = aExpectedBaseUrl;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
      return Arrays.asList(
              new Object[][]{
                      {null, null},
                      {"", ""},
                      {"https://www.testurl.de/resource?query=3", "https://www.testurl.de"},
                      {"www.urlohneschema.de/child", "www.urlohneschema.de"},
                      {"http://www.test.de", "http://www.test.de"}
              });
  }

  @Test
  public void getBaseUrlTest() {
    String actual = UrlUtils.getBaseUrl(inputUrl);

      assertThat(actual, equalTo(expectedBaseUrl));
  }
}
