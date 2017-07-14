package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.progress.CrawlerProgress;
import de.mediathekview.mserver.base.progress.CrawlerProgressListener;

import org.apache.commons.lang3.concurrent.ConcurrentUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * A basic crawler task.
 */
public abstract class AbstractCrawler
{
    private static final Logger LOG = LogManager.getLogger(AbstractCrawler.class);
    protected CopyOnWriteArrayList<RecursiveTask<Film>> tasks;
    protected ForkJoinPool forkJoinPool;
    private Collection<CrawlerProgressListener> progressListeners;
    private Collection<MessageListener> messageListeners;
    
    protected Collection<Film> films;

    private AtomicLong maxCount;
    private AtomicLong actualCount;
    private AtomicLong errorCount;

    AbstractCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, CrawlerProgressListener... aProgressListeners)
    {
        forkJoinPool = aForkJoinPool;
        maxCount = new AtomicLong(0);
        actualCount = new AtomicLong(0);
        
        progressListeners = new ArrayList<>();
        progressListeners.addAll(Arrays.asList(aProgressListeners));
        
        messageListeners = new ArrayList<>();
        messageListeners.addAll(aMessageListeners);
        
        films = new ArrayList<>();
    }

    protected abstract Sender getSender();
    protected abstract void startCrawling();

    void updateProgress()
    {
        CrawlerProgress progress = new CrawlerProgress(maxCount.get(),actualCount.get(),errorCount.get());
        progressListeners.parallelStream().forEach(l -> l.updateCrawlerProgess(getSender(),progress));
    }
    
    void printMessage(Message aMessage, Object... args)
    {
        messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage,args));
    }
    
    
    public void start()
    {
        printMessage(ServerMessages.CRAWLER_START,getSender());
        startCrawling();
        //films.addAll(forkJoinPool.invokeAll(tasks).stream().map(RecursiveTask::join).collect(Collectors.toList()));
        
    }
    

    
}
