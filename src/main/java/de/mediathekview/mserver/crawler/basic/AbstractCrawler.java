package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mlib.progress.CrawlerProgress;
import de.mediathekview.mlib.progress.CrawlerProgressListener;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A basic crawler task.
 */
public abstract class AbstractCrawler implements Callable<Set<Film>>
{
    protected final MServerBasicConfigDTO config;
    protected ForkJoinPool forkJoinPool;
    private Collection<CrawlerProgressListener> progressListeners;
    private Collection<MessageListener> messageListeners;


    protected RecursiveTask<Set<Film>> filmTask;
    protected Set<Film> films;

    private AtomicLong maxCount;
    private AtomicLong actualCount;
    private AtomicLong errorCount;

    public AbstractCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<CrawlerProgressListener> aProgressListeners)
    {
        forkJoinPool = aForkJoinPool;
        maxCount = new AtomicLong(0);
        actualCount = new AtomicLong(0);
        errorCount = new AtomicLong(0);

        progressListeners = aProgressListeners;

        messageListeners = aMessageListeners;

        config = MServerConfigManager.getInstance().getConfig(getSender());

        films = ConcurrentHashMap.newKeySet();
    }

    public abstract Sender getSender();

    protected abstract RecursiveTask<Set<Film>> createCrawlerTask();

    public long incrementAndGetActualCount()
    {
        return actualCount.incrementAndGet();
    }

    public long incrementAndGetMaxCount()
    {
        return maxCount.incrementAndGet();
    }

    public long incrementAndGetErrorCount()
    {
        return errorCount.incrementAndGet();
    }

    public void updateProgress()
    {
        CrawlerProgress progress = new CrawlerProgress(maxCount.get(), actualCount.get(), errorCount.get());
        progressListeners.parallelStream().forEach(l -> l.updateCrawlerProgess(getSender(), progress));
    }

    private void printMessage(Message aMessage, Object... args)
    {
        messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
    }

    public void printErrorMessage()
    {
        printMessage(ServerMessages.CRAWLER_ERROR, getSender());
    }


    @Override
    public Set<Film> call()
    {
        TimeoutTask timeoutRunner = new TimeoutTask(config.getMaximumCrawlDurationInMinutes())
        {
            @Override
            public void shutdown()
            {
                forkJoinPool.shutdownNow();
                printMessage(ServerMessages.CRAWLER_TIMEOUT, getSender().getName());
            }
        };
        timeoutRunner.start();

        printMessage(ServerMessages.CRAWLER_START, getSender());
        LocalTime startTime = LocalTime.now();

        updateProgress();
        filmTask = createCrawlerTask();
        films.addAll(forkJoinPool.invoke(filmTask));

        LocalTime endTime = LocalTime.now();
        CrawlerProgress progress = new CrawlerProgress(maxCount.get(), actualCount.get(), errorCount.get());
        timeoutRunner.stopTimeout();
        printMessage(ServerMessages.CRAWLER_END, getSender(), Duration.between(startTime, endTime).toMinutes(), actualCount.get(), errorCount.get(), progress.calcActualErrorQuoteInPercent());
        return films;
    }


    public void stop()
    {
        filmTask.cancel(true);
    }


}
