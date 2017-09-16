package de.mediathekview.mserver.crawler.basic;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mlib.progress.Progress;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

/**
 * A basic crawler task.
 */
public abstract class AbstractCrawler implements Callable<Set<Film>>
{
    protected final MServerBasicConfigDTO config;
    protected ForkJoinPool forkJoinPool;
    private final Collection<SenderProgressListener> progressListeners;
    private final Collection<MessageListener> messageListeners;

    protected RecursiveTask<Set<Film>> filmTask;
    protected Set<Film> films;

    private final AtomicLong maxCount;
    private final AtomicLong actualCount;
    private final AtomicLong errorCount;

    public AbstractCrawler(final ForkJoinPool aForkJoinPool, final Collection<MessageListener> aMessageListeners,
            final Collection<SenderProgressListener> aProgressListeners)
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
        final Progress progress = new Progress(maxCount.get(), actualCount.get(), errorCount.get());
        progressListeners.parallelStream().forEach(l -> l.updateProgess(getSender(), progress));
    }

    protected void printMessage(final Message aMessage, final Object... args)
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
        final TimeoutTask timeoutRunner = new TimeoutTask(config.getMaximumCrawlDurationInMinutes())
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
        final LocalTime startTime = LocalTime.now();

        updateProgress();
        filmTask = createCrawlerTask();
        films.addAll(forkJoinPool.invoke(filmTask));

        final LocalTime endTime = LocalTime.now();
        final Progress progress = new Progress(maxCount.get(), actualCount.get(), errorCount.get());
        timeoutRunner.stopTimeout();
        printMessage(ServerMessages.CRAWLER_END, getSender(), Duration.between(startTime, endTime).toMinutes(),
                actualCount.get(), errorCount.get(), progress.calcActualErrorQuoteInPercent());
        return films;
    }

    public void stop()
    {
        filmTask.cancel(true);
    }

}
