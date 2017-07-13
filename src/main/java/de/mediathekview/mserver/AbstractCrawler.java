package de.mediathekview.mserver;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.messages.ServerMessages;
import de.mediathekview.mserver.progress.CrawlerProgress;
import de.mediathekview.mserver.progress.CrawlerProgressListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * A basic crawler task.
 */
public abstract class AbstractCrawler<S extends Sender>
{
    private static final Logger LOG = LogManager.getLogger(AbstractCrawler.class);
    private ConcurrentLinkedQueue<RecursiveTask<Film>> tasks;
    private ForkJoinPool forkJoinPool;
    private Collection<CrawlerProgressListener> progressListeners;
    private Collection<MessageListener> messageListeners;
    
    private Collection<Film> films;

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

    abstract S getSender();
    abstract void acquireFilmTasks();

    void updateProgress()
    {
        CrawlerProgress progress = new CrawlerProgress(maxCount.get(),actualCount.get(),errorCount.get());
        progressListeners.parallelStream().forEach(l -> l.updateCrawlerProgess(getSender(),progress));
    }
    
    void printMessage(Message aMessage, Object... args)
    {
        messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage,args));
    }
    
    ForkJoinPool getForkJoinPool()
    {
        return forkJoinPool;
    }
    
     ConcurrentLinkedQueue<RecursiveTask<Film>> getTasks()
     {
         return tasks;
     }
    
    public void start()
    {
        printMessage(ServerMessages.CRAWLER_START,getSender());
        acquireFilmTasks();
        
        //films.addAll(forkJoinPool.invokeAll(tasks).stream().map(RecursiveTask::join).collect(Collectors.toList()));
        
    }
    

    
}
