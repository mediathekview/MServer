/*
 * BrGetClipDetailsTaskTest.java
 *
 * Projekt    : MServer
 * erstellt am: 25.12.2017
 * Autor      : Sascha
 *
 */
package de.mediathekview.mserver.crawler.br.tasks;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.BrTestHelper;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;
import org.junit.Rule;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class BrGetClipDetailsTestTask {

  @Rule public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().dynamicPort());

  // @Test Not yet a Testcase
  public void test()
      throws IOException, URISyntaxException, InterruptedException, ExecutionException {
    final ClassLoader classLoader = getClass().getClassLoader();
    final String expectedJSONresult =
        new String(
            Files.readAllBytes(
                Paths.get(
                    Objects.requireNonNull(
                            classLoader.getResource(
                                "de/mediathekview/mserver/crawler/br/tasks/filmCountResultGraphQL.json"))
                        .toURI())));

    wireMockRule.stubFor(
        post(urlEqualTo("/myBrRequets"))
            .willReturn(
                aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(expectedJSONresult)));

    final ForkJoinPool mainPool = new ForkJoinPool();

    final BrCrawler crawler = BrTestHelper.getTestCrawler("MServer-JUnit-Config.yaml", mainPool);

    final Queue<BrID> clipQueue = new ConcurrentLinkedQueue<>();

    final BrID testId = new BrID(BrClipType.ITEM, "av:591ab1beea223f001260f462");
    // BrID testId = new BrID(BrClipType.PROGRAMME, "av:5a0603ce8c16b90012f4bc49");
    clipQueue.add(testId);

    final BrGetClipDetailsTask clipDetails = new BrGetClipDetailsTask(crawler, clipQueue);

    final Set<Film> resultSet = clipDetails.compute();

    System.out.println(resultSet.iterator().next());
  }
}
