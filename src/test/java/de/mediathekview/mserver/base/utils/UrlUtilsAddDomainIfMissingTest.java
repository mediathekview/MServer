package de.mediathekview.mserver.base.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsAddDomainIfMissingTest {
  private static final String DOMAIN = "https://mydomain.de";

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

  @MethodSource("data")
  @ParameterizedTest
  void addDomainIfMissingTest(final String inputUrl, final String expectedBaseUrl) {
    final String actual = UrlUtils.addDomainIfMissing(inputUrl, DOMAIN);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
