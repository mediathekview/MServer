package de.mediathekview.mserver.crawler.sr.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.sr.SrCrawler;
import de.mediathekview.mserver.crawler.sr.SrTopicUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

public abstract class SrTaskTestBase extends WireMockTestBase {

  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  protected SrCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new SrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }

  protected Queue<SrTopicUrlDTO> createCrawlerUrlDto(final String aTheme, final String aUrl) {
    final Queue<SrTopicUrlDTO> input = new ConcurrentLinkedQueue<>();
    input.add(new SrTopicUrlDTO(aTheme, aUrl));
    return input;
  }
}
