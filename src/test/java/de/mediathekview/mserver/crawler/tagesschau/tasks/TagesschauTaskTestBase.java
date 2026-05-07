package de.mediathekview.mserver.crawler.tagesschau.tasks;

import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.tagesschau.TagesschauCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class TagesschauTaskTestBase extends WireMockTestBase {

  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  protected TagesschauCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new TagesschauCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
