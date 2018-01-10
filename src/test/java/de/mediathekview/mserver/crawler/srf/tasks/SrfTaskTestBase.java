package de.mediathekview.mserver.crawler.srf.tasks;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import org.junit.Rule;

public abstract class SrfTaskTestBase {
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8589);

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
  
  protected ConcurrentLinkedQueue<CrawlerUrlDTO> createCrawlerUrlDto(String aRequestUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO("http://localhost:8589" + aRequestUrl));
    return input;
  }
  
  protected SrfCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>() ;
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    
    SrfCrawler crawler = new SrfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
    return crawler;
  }
  
  protected void setupUrl(String aRequestUrl, String aResponseFile) {
    String jsonBody = FileReader.readFile(aResponseFile);
    wireMockRule.stubFor(get(urlEqualTo(aRequestUrl))
            .willReturn(aResponse()
                    .withHeader("Content-Type", "application/json")
                    .withStatus(200)
                    .withBody(jsonBody)));
  }
}
