package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.crawler.funk.tasks.NexxCloudSessionInitiationTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

public class NexxCloudSessionInitiationTaskTest extends FunkTaskTestBase {

  private FunkCrawler crawler;

  @Test
  public void testSessionInitated() {
    final String requestUrl = "/v3/741/session/init";
    setupSuccessfulJsonPostResponse(requestUrl, "/funk/nexx_cloud_session_init.json", 201);

    final Long actual = executeTask();

    assertThat(actual, notNullValue());
    assertThat(actual, equalTo(3155618042501156672L));
  }

  @Test
  public void testSessionInitiationNotAllowed() {
    final String requestUrl = "/v3/741/session/init";

    wireMockServer.stubFor(
        post(urlEqualTo(requestUrl))
            .willReturn(
                aResponse()
                    .withStatus(403)
                    .withBody(
                        "{\n"
                            + "  \"metadata\": {\n"
                            + "    \"status\": 403,\n"
                            + "    \"apiversion\": \"3.0.22\",\n"
                            + "    \"processingtime\": 0.00018906593322753906,\n"
                            + "    \"calledwith\": \"\\/session\\/init\",\n"
                            + "    \"errorhint\": \"invalidsession\"\n"
                            + "  }\n"
                            + "}")));

    final Long actual = executeTask();
    assertThat(actual, nullValue());
  }

  @Before
  public void setUp() throws MalformedURLException {
    crawler = createCrawler();
    rootConfig
        .getConfig()
        .putCrawlerUrl(
            CrawlerUrlType.NEXX_CLOUD_API_URL, new URL(getWireMockBaseUrlSafe() + "/v3/741"));
  }

  @After
  public void tearDown() {
    CrawlerUrlType.NEXX_CLOUD_API_URL
        .getDefaultUrl()
        .ifPresent(
            url ->
                crawler
                    .getRuntimeConfig()
                    .getCrawlerURLs()
                    .put(CrawlerUrlType.NEXX_CLOUD_API_URL, url));
  }

  private Long executeTask() {
    return new NexxCloudSessionInitiationTask(crawler).call();
  }
}
