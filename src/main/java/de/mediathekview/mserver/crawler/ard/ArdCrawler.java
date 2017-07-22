package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.LogMessageListener;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.CategoriesAZ;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.progress.CrawlerProgressListener;
import de.mediathekview.mserver.base.progress.listeners.ProgressLogMessageListener;
import de.mediathekview.mserver.crawler.AbstractCrawler;
import de.mediathekview.mserver.crawler.ard.tasks.ArdSendungenOverviewPageCrawler;
import de.mediathekview.mserver.crawler.ard.tasks.ArdSendungsfolgenOverviewPageCrawler;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ArdCrawler extends AbstractCrawler
{
    public static final String ARD_BASE_URL = "http://www.ardmediathek.de";
    private static final String ARD_CATEGORY_BASE_URL = ARD_BASE_URL + "/tv/sendungen-a-z?buchstabe=%s";
    private static final String ARD_DAY_BASE_URL = ARD_BASE_URL + "/tv/sendungVerpasst?tag=%d";

    public ArdCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, CrawlerProgressListener... aProgressListeners)
    {
        super(aForkJoinPool, aMessageListeners, aProgressListeners);

    }

    @Override
    public Sender getSender()
    {
        return Sender.ARD;
    }

    protected Collection<RecursiveTask<LinkedHashSet<Film>>> createCrawlerTasks()
    {
        final RecursiveTask<LinkedHashSet<RecursiveTask<LinkedHashSet<Film>>>> categoriesTask = createCategoriesOverviewPageCrawler();
        final RecursiveTask<LinkedHashSet<RecursiveTask<LinkedHashSet<Film>>>> daysTask = createDaysOverviewPageCrawler();
        forkJoinPool.execute(categoriesTask);
        forkJoinPool.execute(daysTask);

        Collection<RecursiveTask<LinkedHashSet<Film>>> crawlerTasks = new ArrayList<>();
        crawlerTasks.addAll(categoriesTask.join());
        crawlerTasks.addAll(daysTask.join());

        return crawlerTasks;
    }

    private RecursiveTask<LinkedHashSet<RecursiveTask<LinkedHashSet<Film>>>> createCategoriesOverviewPageCrawler()
    {
        ConcurrentLinkedQueue<String> categoryUrlsToCrawl = new ConcurrentLinkedQueue<>();
        Arrays.stream(CategoriesAZ.values()).map(c -> String.format(ARD_CATEGORY_BASE_URL, c.getKey())).forEach(categoryUrlsToCrawl::offer);
        return new ArdSendungenOverviewPageCrawler(this, categoryUrlsToCrawl);
    }

    private RecursiveTask<LinkedHashSet<RecursiveTask<LinkedHashSet<Film>>>> createDaysOverviewPageCrawler()
    {
        ConcurrentLinkedQueue<String> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < config.getMaximumDaysForSendungVerpasstSection(); i++)
        {
            dayUrlsToCrawl.offer(String.format(ARD_DAY_BASE_URL, i));
        }
        return new ArdSendungsfolgenOverviewPageCrawler(this, dayUrlsToCrawl);
    }
}
