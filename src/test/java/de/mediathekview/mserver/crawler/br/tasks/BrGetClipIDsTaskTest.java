/*
 * BrGetClipIDsTaskTest.java
 *
 * Projekt    : MServer
 * erstellt am: 13.12.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.crawler.br.tasks;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.BrTestHelper;
import de.mediathekview.mserver.crawler.br.data.BrID;
import org.junit.Rule;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public class BrGetClipIDsTaskTest {

  @Rule public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  // @Test Not yet a Teastcase
  public void test() throws Exception {
    final ClassLoader classLoader = getClass().getClassLoader();
    final String expectedJSONresult =
        new String(
            Files.readAllBytes(
                Paths.get(
                    classLoader
                        .getResource(
                            "de/mediathekview/mserver/crawler/br/tasks/filmCountResultGraphQL.json")
                        .toURI())));

    wireMockRule.stubFor(
        post(urlEqualTo("/myBrRequets"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(expectedJSONresult)));

    final BrCrawler crawler = BrTestHelper.getTestCrawler("MServer-JUnit-Config.yaml");

    final BrGetClipIDsTask clipIds = new BrGetClipIDsTask(crawler);
    final ExecutorService lassLaufen = Executors.newSingleThreadExecutor();
    final Set<BrID> graphqlJsonResult = lassLaufen.submit(clipIds).get();
  }
}
