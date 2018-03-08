package de.mediathekview.mserver.base.utils;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class UrlUtilsTestAddProtocolIfMissing {

  private static final String PROTOCOL = "https:";

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][]{
        {null, null},
        {"", ""},
        {"https://www.testurl.de/resource?query=3", "https://www.testurl.de/resource?query=3"},
        {"www.urlohneschema.de", "www.urlohneschema.de"},
        {"/child/sub", "/child/sub"},
        {"//www.mydomain.de/child/sub", PROTOCOL + "//www.mydomain.de/child/sub"}
    });
  }

  private final String inputUrl;
  private final String expectedBaseUrl;

  public UrlUtilsTestAddProtocolIfMissing(String aInputUrl, String aExpectedBaseUrl) {
    inputUrl = aInputUrl;
    expectedBaseUrl = aExpectedBaseUrl;
  }

  @Test
  public void addProtocolIfMissingTest() {
    String actual = UrlUtils.addProtocolIfMissing(inputUrl, PROTOCOL);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
