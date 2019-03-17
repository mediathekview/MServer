package de.mediathekview.mserver.crawler.hr.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.hr.HrCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class HrTaskTestBase extends WireMockTestBase {

    protected MServerConfigManager rootConfig =
            MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");

  protected HrCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>();
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new HrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
}
