package mServer.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import mServer.crawler.progress.CrawlerProgress;
import mServer.crawler.progress.CrawlerProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A basic crawler task.
 */
public abstract class AbstractCrawler<S extends Sender>
{
    private static final Logger LOG = LogManager.getLogger(AbstractCrawler.class);
    private ConcurrentLinkedQueue<RecursiveTask<Film>> filmTasks;
    private ForkJoinPool forkJoinPool;
    private Collection<CrawlerProgressListener> listeners;

    private AtomicLong maxCount;
    private AtomicLong actualCount;
    private AtomicLong errorCount;

    AbstractCrawler(ForkJoinPool aForkJoinPool, CrawlerProgressListener... aListeners)
    {
        forkJoinPool = aForkJoinPool;
        maxCount = new AtomicLong(0);
        actualCount = new AtomicLong(0);
        listeners= new ArrayList<>();
        listeners.addAll(Arrays.asList(aListeners));
    }

    abstract S getSender();

    void updateProgress()
    {
        CrawlerProgress progress = new CrawlerProgress(maxCount.get(),actualCount.get(),errorCount.get());
        listeners.parallelStream().forEach(l -> l.updateCrawlerProgess(progress));
    }
}
