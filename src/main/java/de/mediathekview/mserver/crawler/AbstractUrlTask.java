package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * Recursively crawls a Website.
 */
public abstract class AbstractUrlTask<T> extends RecursiveTask<LinkedHashSet<T>>
{
    private static final Logger LOG = LogManager.getLogger(AbstractUrlTask.class);
    private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN = "Something terrible happened while crawl the %s page \"%s\".";

    protected final ConcurrentLinkedQueue<String> urlsToCrawl;
    protected final MServerBasicConfigDTO config;
    protected AbstractCrawler crawler;

    protected LinkedHashSet<T> filmTasks;

    public AbstractUrlTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        crawler = aCrawler;
        urlsToCrawl = aUrlsToCrawl;
        filmTasks = new LinkedHashSet<>();
        config = MServerConfigManager.getInstance().getConfig(crawler.getSender());
    }

    @Override
    protected LinkedHashSet<T> compute()
    {
        final ConcurrentLinkedQueue<String> urlsToCrawlSubset = new ConcurrentLinkedQueue<>();
        for(int i=0; i <urlsToCrawl.size() && i < config.getMaximumUrlsPerTask(); i++)
        {
            urlsToCrawlSubset.offer(urlsToCrawl.poll());
        }

        if (urlsToCrawl.size() <= config.getMaximumUrlsPerTask())
        {
            crawlPage(urlsToCrawlSubset);
        } else
        {
            AbstractUrlTask<T> otherTask = createNewOwnInstance();
            otherTask.fork();
            crawlPage(urlsToCrawlSubset);
            filmTasks.addAll(otherTask.join());
        }
        return filmTasks;
    }

    protected abstract AbstractUrlTask<T> createNewOwnInstance();

    private void crawlPage(ConcurrentLinkedQueue<String> aUrls)
    {
        for(String url : aUrls)
        {
            try
            {
                Document document = Jsoup.connect(url).get();
                processDocument(url, document);
            } catch (IOException ioException)
            {
                LOG.fatal(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN, crawler.getSender().getName(), url), ioException);
                crawler.printErrorMessage();
                throw new RuntimeException(ioException);
            }
        }
    }

    protected abstract void processDocument(final String aUrl, final Document aDocument);

}
