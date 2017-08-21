package de.mediathekview.mserver.crawler.basic;

import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

/**
 * Recursively crawls a Website.
 */
public abstract class AbstractUrlTask<T,D extends CrawlerUrlsDTO> extends RecursiveTask<Set<T>>
{
    private static final Logger LOG = LogManager.getLogger(AbstractUrlTask.class);
    private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN = "Something terrible happened while crawl the %s page \"%s\".";

    protected final ConcurrentLinkedQueue<D> urlsToCrawl;
    protected final MServerBasicConfigDTO config;
    protected AbstractCrawler crawler;

    protected Set<T> taskResults;

    public AbstractUrlTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<D> aUrlToCrawlDTOs)
    {
        crawler = aCrawler;
        urlsToCrawl = aUrlToCrawlDTOs;
        taskResults = ConcurrentHashMap.newKeySet();
        config = MServerConfigManager.getInstance().getConfig(crawler.getSender());
    }

    @Override
    protected Set<T> compute()
    {
        final ConcurrentLinkedQueue<D> urlsToCrawlSubset = new ConcurrentLinkedQueue<>();
        for(int i=0; i <urlsToCrawl.size() && i < config.getMaximumUrlsPerTask(); i++)
        {
            urlsToCrawlSubset.offer(urlsToCrawl.poll());
        }

        if (urlsToCrawl.size() <= config.getMaximumUrlsPerTask())
        {
            crawlPage(urlsToCrawlSubset);
        } else
        {
            AbstractUrlTask<T,D> otherTask = createNewOwnInstance();
            otherTask.fork();
            crawlPage(urlsToCrawlSubset);
            taskResults.addAll(otherTask.join());
        }
        return taskResults;
    }

    protected abstract AbstractUrlTask<T,D> createNewOwnInstance();

    private void crawlPage(ConcurrentLinkedQueue<D> aUrls)
    {
        D urlDTO;
        while((urlDTO = aUrls.poll()) != null)
        {
            try
            {
                Document document = Jsoup.connect(urlDTO.getUrl()).get();
                processDocument(urlDTO, document);
            } catch (IOException ioException)
            {
                LOG.fatal(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN, crawler.getSender().getName(), urlDTO.getUrl()), ioException);
                crawler.printErrorMessage();
                throw new RuntimeException(ioException);
            }
        }
    }

    protected abstract void processDocument(final D aUrlDTO, final Document aDocument);

}
