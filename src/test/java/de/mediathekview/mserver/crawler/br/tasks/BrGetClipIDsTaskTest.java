/*
 * BrGetClipIDsTaskTest.java
 * 
 * Projekt    : MServer
 * erstellt am: 13.12.2017
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.br.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.Rule;
import org.junit.Test;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.crawler.br.BrTestHelper;
import de.mediathekview.mserver.crawler.br.data.BrID;

public class BrGetClipIDsTaskTest {

  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8589); // No-args constructor defaults to port 8080

  // @Test Not yet a Teastcase
  public void test() throws Exception {
    ClassLoader classLoader = getClass().getClassLoader();
    String expectedJSONresult = new String(Files.readAllBytes(Paths.get(classLoader.getResource("de/mediathekview/mserver/crawler/br/tasks/filmCountResultGraphQL.json").toURI()))); 
    
    wireMockRule.stubFor(post(urlEqualTo("/myBrRequets"))
                .willReturn(aResponse()
                .withHeader("Content-Type", "application/json")
                .withStatus(200)
                .withBody(expectedJSONresult)));
    
    
    BrCrawler crawler = BrTestHelper.getTestCrawler("MServer-JUnit-Config.yaml");
    
    BrGetClipIDsTask clipIds = new BrGetClipIDsTask(crawler);
    ExecutorService lassLaufen = Executors.newSingleThreadExecutor();
    Set<BrID> graphqlJsonResult = lassLaufen.submit(clipIds).get();
    
  }

}
