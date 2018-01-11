package de.mediathekview.mserver.crawler.srf.tasks;

import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.srf.SrfCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import de.mediathekview.mserver.testhelper.WireMockTestBase;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public abstract class SrfTaskTestBase extends WireMockTestBase {
  
  protected MServerConfigManager rootConfig = MServerConfigManager.getInstance("MServer-JUnit-Config.yaml");
  
  protected SrfCrawler createCrawler() {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    Collection<MessageListener> nachrichten = new ArrayList<>() ;
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    
    SrfCrawler crawler = new SrfCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
    return crawler;
  } 
}
