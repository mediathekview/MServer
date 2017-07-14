package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
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
public abstract class AbstractUrlTask extends RecursiveTask<LinkedHashSet<Film>>
{
    private static final Logger LOG = LogManager.getLogger(AbstractUrlTask.class);
    private static final String LOAD_DOCUMENT_ERRORTEXTPATTERN = "Something terrible happened while crawl the %s page \"%s\".";

    protected final ConcurrentLinkedQueue<String> urlsToCrawl;
    protected AbstractCrawler crawler;

    protected LinkedHashSet<Film> crawledFilms;

    public AbstractUrlTask(AbstractCrawler aCrawler, ConcurrentLinkedQueue<String> aUrlsToCrawl)
    {
        crawler = aCrawler;
        urlsToCrawl = aUrlsToCrawl;
        crawledFilms = new LinkedHashSet<>();
    }

    @Override
    protected LinkedHashSet<Film> compute()
    {
        final String urlToCrawl = urlsToCrawl.poll();

        if (urlsToCrawl.isEmpty())
        {
            crawlPage(urlToCrawl);
        } else
        {
            AbstractUrlTask otherTask = createNewOwnInstance();
            otherTask.fork();
            crawlPage(urlToCrawl);
            crawledFilms.addAll(otherTask.join());
        }

        return crawledFilms;
    }

    protected abstract AbstractUrlTask createNewOwnInstance();

    private void crawlPage(String aUrl)
    {
        try
        {
            Document document = Jsoup.connect(aUrl).get();
            processDocument(aUrl,document);
        } catch (IOException ioException)
        {
            LOG.fatal(String.format(LOAD_DOCUMENT_ERRORTEXTPATTERN,crawler.getSender().getName(), aUrl), ioException);
            crawler.printErrorMessage();
            throw new RuntimeException(ioException);
        }
    }

    protected abstract void processDocument(final String aUrl, final Document aDocument);

}
