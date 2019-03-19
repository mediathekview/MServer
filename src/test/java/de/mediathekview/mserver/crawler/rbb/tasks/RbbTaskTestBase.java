package de.mediathekview.mserver.crawler.rbb.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.rbb.RbbCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public abstract class RbbTaskTestBase extends WireMockTestBase {
    protected MServerConfigManager rootConfig =
            MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected RbbCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new RbbCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
