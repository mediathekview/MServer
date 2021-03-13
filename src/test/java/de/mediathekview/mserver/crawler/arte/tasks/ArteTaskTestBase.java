package de.mediathekview.mserver.crawler.arte.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.arte.ArteCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public abstract class ArteTaskTestBase extends WireMockTestBase {
  protected MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");

  protected ArteCrawler createCrawler() {
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new ArteCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
