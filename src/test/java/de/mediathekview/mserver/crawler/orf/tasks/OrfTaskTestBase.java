package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.orf.OrfCrawler;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

public abstract class OrfTaskTestBase extends WireMockTestBase {
  
  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  public OrfTaskTestBase() {
  }

  protected OrfCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new OrfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  protected ConcurrentLinkedQueue<TopicUrlDTO> createCrawlerUrlDto(String aTheme, String aUrl) {
    ConcurrentLinkedQueue<TopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new TopicUrlDTO(aTheme, aUrl));
    return input;
  }
  
}
