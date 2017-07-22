package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.progress.CrawlerProgress;
import de.mediathekview.mserver.base.progress.CrawlerProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A basic crawler task.
 */
public abstract class AbstractCrawler implements Callable<LinkedHashSet<Film>>
{
    private static final Logger LOG = LogManager.getLogger(AbstractCrawler.class);
    private static final long TIMEOUT_SLEEP_MILLIS = Duration.of(1, ChronoUnit.MINUTES).toMillis();
    protected final MServerBasicConfigDTO config;
    protected ForkJoinPool forkJoinPool;
    private Collection<CrawlerProgressListener> progressListeners;
    private Collection<MessageListener> messageListeners;


    protected LinkedHashSet<RecursiveTask<LinkedHashSet<Film>>> filmTasks;
    protected LinkedHashSet<Film> films;

    private AtomicLong maxCount;
    private AtomicLong actualCount;
    private AtomicLong errorCount;

    public AbstractCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, CrawlerProgressListener... aProgressListeners)
    {
        forkJoinPool = aForkJoinPool;
        maxCount = new AtomicLong(0);
        actualCount = new AtomicLong(0);
        errorCount = new AtomicLong(0);

        progressListeners = new ArrayList<>();
        progressListeners.addAll(Arrays.asList(aProgressListeners));

        messageListeners = new ArrayList<>();
        messageListeners.addAll(aMessageListeners);

        filmTasks = new LinkedHashSet<>();
        config = MServerConfigManager.getInstance().getConfig(getSender());

        films = new LinkedHashSet<>();
    }

    public abstract Sender getSender();

    protected abstract Collection<RecursiveTask<LinkedHashSet<Film>>> createCrawlerTasks();

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
    public LinkedHashSet<Film> call()
    {
        TimeoutRunner timeoutRunner = new TimeoutRunner();
        Thread timeoutRunnerThread = new Thread(timeoutRunner);
        timeoutRunnerThread.start();

        printMessage(ServerMessages.CRAWLER_START, getSender());
        LocalTime startTime = LocalTime.now();

        updateProgress();
        filmTasks.addAll(createCrawlerTasks());
        runFilmTasks();

        LocalTime endTime = LocalTime.now();
        CrawlerProgress progress = new CrawlerProgress(maxCount.get(), actualCount.get(), errorCount.get());
        printMessage(ServerMessages.CRAWLER_END, getSender(), Duration.between(startTime, endTime).toMinutes(), actualCount.get(), errorCount.get(), progress.calcActualErrorQuoteInPercent());
        timeoutRunner.stop();
        return films;
    }

    private void runFilmTasks()
    {
        filmTasks.forEach(forkJoinPool::execute);
        filmTasks.parallelStream().map(RecursiveTask::join).forEach(films::addAll);
    }

    public void stop()
    {
        filmTasks.parallelStream().forEach(t -> t.cancel(true));
    }

    private class TimeoutRunner implements Runnable
    {
        private boolean isRun;

        private TimeoutRunner()
        {
            isRun = true;
        }

        public void stop()
        {
            isRun = false;
        }

        @Override
        public void run()
        {
            LocalDateTime beginTime = LocalDateTime.now();
            while (isRun)
            {
                if (Duration.between(beginTime, LocalDateTime.now()).toMinutes() < config.getMaximumCrawlDurationInMinutes())
                {
                    try
                    {
                        Thread.sleep(TIMEOUT_SLEEP_MILLIS);
                    } catch (InterruptedException interruptedException)
                    {
                        LOG.debug("The crawler TimeoutRunner has been interrupted.", interruptedException);
                    }
                } else
                {
                    forkJoinPool.shutdownNow();
                    printMessage(ServerMessages.CRAWLER_TIMEOUT, getSender().getName());
                }
            }
        }
    }
}
