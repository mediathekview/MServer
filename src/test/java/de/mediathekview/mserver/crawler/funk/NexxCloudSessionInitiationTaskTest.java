package de.mediathekview.mserver.crawler.funk;

import de.mediathekview.mserver.base.config.CrawlerUrlType;
import de.mediathekview.mserver.crawler.funk.tasks.NexxCloudSessionInitiationTask;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.Assert.assertThat;

public class NexxCloudSessionInitiationTaskTest extends FunkTaskTestBase {

  private FunkCrawler crawler;
  private URL oldApiUrl;

  @Test
  public void testSessionInitated() {
    final String requestUrl = "/v3/741/session/init";
    setupSuccessfulJsonPostResponse(
        requestUrl, "/funk/nexx_cloud_session_init.json", Optional.of(201));

    final Long actual = executeTask();

    assertThat(actual, notNullValue());
    assertThat(actual, equalTo(3155618042501156672L));
  }

  @Test
  public void testSessionInitiationNotAllowed() {
    final String requestUrl = "/v3/741/session/init";

    wireMockRule.stubFor(
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
    oldApiUrl = crawler.getRuntimeConfig().getCrawlerURLs().get(CrawlerUrlType.NEXX_CLUD_API_URL);
    crawler
        .getRuntimeConfig()
        .getCrawlerURLs()
        .put(CrawlerUrlType.NEXX_CLUD_API_URL, new URL("http://localhost:8589/v3/741"));
  }

  @After
  public void tearDown() {
    crawler.getRuntimeConfig().getCrawlerURLs().put(CrawlerUrlType.NEXX_CLUD_API_URL, oldApiUrl);
  }

  private Long executeTask() {
    return new NexxCloudSessionInitiationTask(crawler).call();
  }
}
