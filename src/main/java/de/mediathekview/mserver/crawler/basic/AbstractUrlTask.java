package de.mediathekview.mserver.crawler.basic;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import de.mediathekview.mserver.base.config.MServerBasicConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;

/**
 * Recursively crawls a Website.
 */
public abstract class AbstractUrlTask<T, D extends CrawlerUrlsDTO> extends RecursiveTask<Set<T>>
{

    private static final long serialVersionUID = -4077156510484515410L;
    private static final Logger LOG = LogManager.getLogger(AbstractUrlTask.class);
    private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN =
            "Something terrible happened while crawl the %s page \"%s\".";
    private static final String LOAD_DOCUMENT_HTTPERROR = "Some HTTP error happened while crawl the %s page \"%s\".";

    private final ConcurrentLinkedQueue<D> urlsToCrawl;
    protected final MServerBasicConfigDTO config;
    protected AbstractCrawler crawler;

    protected Set<T> taskResults;

    public AbstractUrlTask(final AbstractCrawler aCrawler, final ConcurrentLinkedQueue<D> aUrlToCrawlDTOs)
    {
        crawler = aCrawler;
        urlsToCrawl = aUrlToCrawlDTOs;
        taskResults = ConcurrentHashMap.newKeySet();
        config = MServerConfigManager.getInstance().getConfig(crawler.getSender());
    }

    @Override
    protected Set<T> compute()
    {
        if (urlsToCrawl.size() <= config.getMaximumUrlsPerTask())
        {
            crawlPage(urlsToCrawl);
        }
        else
        {
            final AbstractUrlTask<T, D> rightTask = createNewOwnInstance(createSubSet(urlsToCrawl));
            final AbstractUrlTask<T, D> leftTask = createNewOwnInstance(urlsToCrawl);
            leftTask.fork();
            taskResults.addAll(rightTask.compute());
            taskResults.addAll(leftTask.join());
        }
        return taskResults;
    }

    private ConcurrentLinkedQueue<D> createSubSet(final ConcurrentLinkedQueue<D> aBaseQueue)
    {
        final int halfSize = aBaseQueue.size() / 2;
        final ConcurrentLinkedQueue<D> urlsToCrawlSubset = new ConcurrentLinkedQueue<>();
        for (int i = 0; i < halfSize; i++)
        {
            urlsToCrawlSubset.offer(aBaseQueue.poll());
        }
        return urlsToCrawlSubset;
    }

    protected abstract AbstractUrlTask<T, D> createNewOwnInstance(ConcurrentLinkedQueue<D> aURLsToCrawl);

    private void crawlPage(final ConcurrentLinkedQueue<D> aUrls)
    {
        D urlDTO;
        while ((urlDTO = aUrls.poll()) != null)
        {
            try
            {
                final Document document = Jsoup.connect(urlDTO.getUrl()).get();
                processDocument(urlDTO, document);
            }
            catch (final HttpStatusException httpStatusError)
            {
                LOG.error(String.format(LOAD_DOCUMENT_HTTPERROR, crawler.getSender().getName(), urlDTO.getUrl()));
                crawler.printMessage(ServerMessages.CRAWLER_DOCUMENT_LOAD_ERROR, crawler.getSender().getName(),
                        urlDTO.getUrl(), httpStatusError.getStatusCode());
            }
            catch (final IOException ioException)
            {
                LOG.fatal(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN, crawler.getSender().getName(), urlDTO.getUrl()),
                        ioException);
                crawler.printErrorMessage();
                throw new RuntimeException(ioException);
            }
        }
    }

    protected abstract void processDocument(final D aUrlDTO, final Document aDocument);

}
