/*
 * BrGetClipDetailsTaskTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 25.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.Assert.*;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.BrTestHelper;
import de.mediathekview.mserver.crawler.br.data.BrClipType;
import de.mediathekview.mserver.crawler.br.data.BrID;

public class BrGetClipDetailsTaskTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8589); // No-args constructor defaults to port 8080

  
  //@Test Not yet a Testcase
  public void test() throws IOException, URISyntaxException, InterruptedException, ExecutionException {
    ClassLoader classLoader = getClass().getClassLoader();
    String expectedJSONresult = new String(Files.readAllBytes(Paths.get(classLoader.getResource("de/mediathekview/mserver/crawler/br/tasks/filmCountResultGraphQL.json").toURI()))); 
    
    wireMockRule.stubFor(post(urlEqualTo("/myBrRequets"))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(expectedJSONresult)));
    
    ForkJoinPool mainPool = new ForkJoinPool();
    
    BrCrawler crawler = BrTestHelper.getTestCrawler("MServer-Config.yaml", mainPool);
    
    ConcurrentLinkedQueue<BrID> clipQueue = new ConcurrentLinkedQueue<>();

    BrID testId = new BrID(BrClipType.ITEM, "av:591ab1beea223f001260f462");
    //BrID testId = new BrID(BrClipType.PROGRAMME, "av:5a0603ce8c16b90012f4bc49");
    clipQueue.add(testId);

    BrGetClipDetailsTask clipDetails = new BrGetClipDetailsTask(crawler, clipQueue);
    
    Set<Film> resultSet = clipDetails.compute();
    
    System.out.println(resultSet.iterator().next());
    
  }

}
