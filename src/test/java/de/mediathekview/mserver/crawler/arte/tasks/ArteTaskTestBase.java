package de.mediathekview.mserver.crawler.arte.tasks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.arte.ArteCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class ArteTaskTestBase {

  public static ArteCrawler createCrawler() {
    MServerConfigManager rootConfig = new MServerConfigManager("MServer-JUnit-Config.yaml");
    final ForkJoinPool forkJoinPool = new ForkJoinPool();
    final Collection<MessageListener> nachrichten = new ArrayList<>();
    final Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    return new ArteCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
  
  public ArteTaskTestBase(){}
}
