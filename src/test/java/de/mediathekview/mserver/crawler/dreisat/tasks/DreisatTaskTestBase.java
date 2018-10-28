package de.mediathekview.mserver.crawler.dreisat.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.dreisat.DreiSatCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public abstract class DreisatTaskTestBase extends WireMockTestBase {

  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected DreiSatCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();

    return new DreiSatCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
