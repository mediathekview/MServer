package de.mediathekview.mserver.crawler.srf.tasks;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import org.junit.Rule;

public abstract class SrfTaskTestBase {
  
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(8589);

  protected ConcurrentLinkedQueue<CrawlerUrlDTO> createCrawlerUrlDto(String aRequestUrl) {
    ConcurrentLinkedQueue<CrawlerUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new CrawlerUrlDTO("http://localhost:8589" + aRequestUrl));
    return input;
  }
  
  protected SrfCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>() ;
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
    
    SrfCrawler crawler = new SrfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
    return crawler;
  }
}
