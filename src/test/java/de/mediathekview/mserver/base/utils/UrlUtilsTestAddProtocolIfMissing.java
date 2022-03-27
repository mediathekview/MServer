package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsTestAddProtocolIfMissing {

  private static final String PROTOCOL = "https:";
  private final String inputUrl;
  private final String expectedBaseUrl;

  public UrlUtilsTestAddProtocolIfMissing(final String aInputUrl, final String aExpectedBaseUrl) {
    inputUrl = aInputUrl;
    expectedBaseUrl = aExpectedBaseUrl;
  }

  @Parameterized.Parameters
  public static Collection<String[]> data() {
    return Arrays.asList(
        new String[][] {
          {null, null},
          {"", ""},
          {"https://www.testurl.de/resource?query=3", "https://www.testurl.de/resource?query=3"},
          {"http://www.testurl.de/resource?query=3", "http://www.testurl.de/resource?query=3"},
          {"www.urlohneschema.de", PROTOCOL + "//www.urlohneschema.de"},
          {"/child/sub", "/child/sub"},
          {"//www.mydomain.de/child/sub", PROTOCOL + "//www.mydomain.de/child/sub"}
        });
  }

  @Test
  public void addProtocolIfMissingTest() {
    final String actual = UrlUtils.addProtocolIfMissing(inputUrl, PROTOCOL);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
