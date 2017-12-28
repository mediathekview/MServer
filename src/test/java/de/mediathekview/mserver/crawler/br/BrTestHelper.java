/*
 * BrCrawlerHelper.java
 * 
 * Projekt    : MServer
 * erstellt am: 13.12.2017
 * Autor      : Sascha
 * 
 * (c) 2017 by Sascha Wiegandt
 */
package de.mediathekview.mserver.crawler.br;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ForkJoinPool;
import org.apache.commons.lang3.StringUtils;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class BrTestHelper {

  public static final String DEFAULT_TEST_CONFIG_FILENAME = "MServer-JUnit-Config.yaml";
  
  public static BrCrawler getTestCrawler(String configFilename) {
    ForkJoinPool forkJoinPool = new ForkJoinPool();
    return getTestCrawler(configFilename, forkJoinPool);
  }
  
  public static BrCrawler getTestCrawler(String configFilename, ForkJoinPool forkJoinPool) {
    Collection<MessageListener> nachrichten = new ArrayList<>() ;
    MessageListener nachricht = new MessageListener() {
        
        @Override
        public void consumeMessage(Message arg0, Object... arg1) {
            // TODO Auto-generated method stub
            
        }
    }; 
    nachrichten.add(nachricht);
    
    Collection<SenderProgressListener> fortschritte = new ArrayList<>();
    SenderProgressListener fortschritt = new SenderProgressListener() {
        
        @Override
        public void updateProgess(Sender aSender, Progress aCrawlerProgress) {
            // TODO Auto-generated method stub
            
        }
    }; 
    fortschritte.add(fortschritt);

    if(StringUtils.isBlank(configFilename)) {
      configFilename = DEFAULT_TEST_CONFIG_FILENAME;
    }
    
    MServerConfigManager rootConfig = MServerConfigManager.getInstance(configFilename);
    
    return new BrCrawler(forkJoinPool, nachrichten, fortschritte, rootConfig);
  }
  

}
