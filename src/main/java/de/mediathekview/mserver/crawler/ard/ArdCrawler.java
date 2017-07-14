package de.mediathekview.mserver.crawler.ard;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.progress.CrawlerProgressListener;

public class ArdCrawler extends AbstractCrawler {
   
   public ArdCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, CrawlerProgressListener... aProgressListeners) {
        super(aForkJoinPool,aMessageListeners,aProgressListeners);
    }

@Override
protected Sender getSender() {
    return Sender.ARD;
}

@Override
protected void startCrawling() {
    // TODO Auto-generated method stub
    
}
}
