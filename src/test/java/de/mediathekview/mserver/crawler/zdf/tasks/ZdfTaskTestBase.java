package de.mediathekview.mserver.crawler.zdf.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.zdf.ZdfCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public abstract class ZdfTaskTestBase extends WireMockTestBase {

  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  protected ZdfCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ZdfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
