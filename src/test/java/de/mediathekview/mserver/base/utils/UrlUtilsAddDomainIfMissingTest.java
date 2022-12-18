package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsAddDomainIfMissingTest {
  private static final String DOMAIN = "https://mydomain.de";
  private final String inputUrl;
  private final String expectedBaseUrl;

  public UrlUtilsAddDomainIfMissingTest(final String aInputUrl, final String aExpectedBaseUrl) {
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
          {"www.urlohneschema.de", "www.urlohneschema.de"},
          {"/child/sub", DOMAIN + "/child/sub"}
        });
  }

  @Test
  public void addDomainIfMissingTest() {
    final String actual = UrlUtils.addDomainIfMissing(inputUrl, DOMAIN);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
