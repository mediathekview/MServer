package de.mediathekview.mserver.base.utils;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_LENGTH;
import static jakarta.ws.rs.core.HttpHeaders.CONTENT_TYPE;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FileSizeDeterminerTest {
  private static final String TEST_FILE_NAME = "FileSizeDeterminerTest.txt";
  private static final String TEST_FILE_URL = "/" + TEST_FILE_NAME;
  private static final WireMockServer wireMockServer = new WireMockServer(options().dynamicPort());

  @BeforeAll
  static void setUpWiremock() {
    wireMockServer.stubFor(
        head(urlEqualTo("/" + TEST_FILE_NAME))
            .willReturn(
                aResponse().withStatus(200).withHeader(CONTENT_LENGTH, "5643").withHeader(CONTENT_TYPE, "text/html")));
  }

  @BeforeEach
  void startWireMock() {
    wireMockServer.start();
  }

  @AfterEach
  void stopWireMock() {
    wireMockServer.stop();
  }

  @Test
  void testGetFileSize() {
    AssertionsForClassTypes.assertThat(getClassUnderTest().getRequestInfo(wireMockServer.baseUrl() + TEST_FILE_URL).size()).isEqualTo(5643L);
  }
  
  @Test
  void testGetStatusCode() {
    AssertionsForClassTypes.assertThat(getClassUnderTest().getRequestInfo(wireMockServer.baseUrl() + TEST_FILE_URL).code()).isEqualTo(200);
  }
  
  @Test
  void testGetContentType() {
    AssertionsForClassTypes.assertThat(getClassUnderTest().getRequestInfo(wireMockServer.baseUrl() + TEST_FILE_URL).contentType()).isEqualTo("text/html");
  }

  private FileSizeDeterminer getClassUnderTest() {
    return new FileSizeDeterminer();
  }
}
