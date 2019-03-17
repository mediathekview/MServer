package de.mediathekview.mserver.base.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(Parameterized.class)
public class UrlUtilsTestAddDomainIfMissing {
  private static final String DOMAIN = "https://mydomain.de";
    private final String inputUrl;
    private final String expectedBaseUrl;

    public UrlUtilsTestAddDomainIfMissing(String aInputUrl, String aExpectedBaseUrl) {
        inputUrl = aInputUrl;
        expectedBaseUrl = aExpectedBaseUrl;
    }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
      return Arrays.asList(
              new Object[][]{
                      {null, null},
                      {"", ""},
                      {"https://www.testurl.de/resource?query=3", "https://www.testurl.de/resource?query=3"},
                      {"www.urlohneschema.de", "www.urlohneschema.de"},
                      {"/child/sub", DOMAIN + "/child/sub"}
              });
  }

  @Test
  public void addDomainIfMissingTest() {
    String actual = UrlUtils.addDomainIfMissing(inputUrl, DOMAIN);

      assertThat(actual, equalTo(expectedBaseUrl));
  }
}
