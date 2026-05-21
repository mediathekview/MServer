package de.mediathekview.mserver.base.utils;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class UrlUtilsAddProtocolIfMissingTest {

  private static final String PROTOCOL = "https:";

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

  @MethodSource("data")
  @ParameterizedTest
  void addProtocolIfMissingTest(final String inputUrl, final String expectedBaseUrl) {
    final String actual = UrlUtils.addProtocolIfMissing(inputUrl, PROTOCOL);

    assertThat(actual, equalTo(expectedBaseUrl));
  }
}
