package de.mediathekview.mserver.crawler.ard;

import org.glassfish.jersey.uri.UriComponent;
import org.glassfish.jersey.uri.UriComponent.Type;
import org.junit.Test;

import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ArdUrlBuilderTest {

  @Test
  public void testWithoutOptionalParameters() {
    final String expectedUrl =
        "https://api.ardmediathek.de/public-gateway?variables="
            + UriComponent.encode("{\"client\":\"test\"}", Type.QUERY_PARAM);

    final String actualUrl =
        new ArdUrlBuilder("https://api.ardmediathek.de/public-gateway", "test").build();

    assertThat(actualUrl, equalTo(expectedUrl));
  }

  @Test
  public void testClipParameter() {
    final String expectedUrl =
        "https://my.api.de?variables="
            + UriComponent.encode(
                "{\"client\":\"test\",\"clipId\":\"jd795kjl8\",\"deviceType\":\"screen\"}",
                Type.QUERY_PARAM);

    final String actualUrl =
        new ArdUrlBuilder("https://my.api.de", "test").addClipId("jd795kjl8", "screen").build();

    assertThat(actualUrl, equalTo(expectedUrl));
  }

  @Test
  public void testShowParameter() {
    final String expectedUrl =
        "https://my.api.de?variables="
            + UriComponent.encode(
                "{\"client\":\"test\",\"showId\":\"dj874hjd7\"}", Type.QUERY_PARAM);

    final String actualUrl =
        new ArdUrlBuilder("https://my.api.de", "test").addShowId("dj874hjd7").build();

    assertThat(actualUrl, equalTo(expectedUrl));
  }

  @Test
  public void testSearchDateParameter() {
    final String expectedUrl =
        "https://my.api.de?variables="
            + UriComponent.encode(
                "{\"client\":\"test\",\"startDate\":\"2018-12-07\"}", Type.QUERY_PARAM);

    final String actualUrl =
        new ArdUrlBuilder("https://my.api.de", "test")
            .addSearchDate(LocalDateTime.of(2018, 12, 7, 12, 55, 36))
            .build();

    assertThat(actualUrl, equalTo(expectedUrl));
  }

  @Test
  public void testSavedQuery() {
    final String expectedUrl =
        "https://my.api.de?variables="
            + UriComponent.encode("{\"client\":\"test\"}", Type.QUERY_PARAM)
            + "&extensions="
            + UriComponent.encode(
                "{\"persistedQuery\":{\"version\":1,\"sha256Hash\":\"s86d73d\"}}",
                Type.QUERY_PARAM);

    final String actualUrl =
        new ArdUrlBuilder("https://my.api.de", "test").addSavedQuery(1, "s86d73d").build();

    assertThat(actualUrl, equalTo(expectedUrl));
  }
}
