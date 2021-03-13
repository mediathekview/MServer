package de.mediathekview.mserver.crawler.dw.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.dw.DwCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public abstract class DwTaskTestBase extends WireMockTestBase {
  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  protected DwCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new DwCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
