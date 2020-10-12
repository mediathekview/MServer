package de.mediathekview.mserver.crawler.wdr.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.wdr.WdrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

public class WdrTaskTestBase extends WireMockTestBase {
  protected MServerConfigManager rootConfig =
      MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected WdrCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new WdrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  protected Queue<TopicUrlDTO> createCrawlerUrlDto(final String aTopic, final String aUrl) {
    final Queue<TopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new TopicUrlDTO(aTopic, aUrl));
    return input;
  }
}
