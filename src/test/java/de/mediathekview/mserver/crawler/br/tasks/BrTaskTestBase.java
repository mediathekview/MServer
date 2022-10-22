package de.mediathekview.mserver.crawler.br.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.br.BrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBaseJUnit5;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class BrTaskTestBase extends WireMockTestBaseJUnit5 {
  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  protected BrCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new BrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
