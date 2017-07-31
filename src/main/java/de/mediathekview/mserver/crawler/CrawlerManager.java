package de.mediathekview.mserver.crawler;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.Message;
import de.mediathekview.mlib.messages.listener.LogMessageListener;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigDTO;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.base.progress.CrawlerProgressListener;
import de.mediathekview.mserver.base.progress.listeners.ProgressLogMessageListener;
import de.mediathekview.mserver.crawler.ard.ArdCrawler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * A manager to control the crawler.
 */
public class CrawlerManager
{
    private static final Logger LOG = LogManager.getLogger(CrawlerManager.class);
    private static CrawlerManager instance;
    private final MServerConfigDTO config;
    private final ForkJoinPool forkJoinPool;
    private final Filmlist filmlist;
    private final ExecutorService executorService;
    private Collection<CrawlerProgressListener> progressListeners;
    private Collection<MessageListener> messageListeners;

    public static CrawlerManager getInstance()
    {
        if (instance == null)
        {
            instance = new CrawlerManager();
        }
        return instance;
    }

    private Map<Sender, AbstractCrawler> crawlerMap;

    private CrawlerManager()
    {
        super();
        config = MServerConfigManager.getInstance().getConfig();
        executorService = Executors.newFixedThreadPool(config.getMaximumCpuThreads());
        forkJoinPool = new ForkJoinPool(config.getMaximumCpuThreads());

        crawlerMap = new EnumMap<>(Sender.class);
        progressListeners = new ArrayList<>();
        messageListeners = new ArrayList<>();
        filmlist = new Filmlist();
        initializeCrawler();
    }

    private void initializeCrawler()
    {
        crawlerMap.put(Sender.ARD, new ArdCrawler(forkJoinPool, messageListeners, progressListeners));
    }

    public void startCrawlerForSender(Sender aSender)
    {
        if (crawlerMap.containsKey(aSender))
        {
            final AbstractCrawler crawler = crawlerMap.get(aSender);
            runCrawlers(crawler);
        } else
        {
            throw new IllegalArgumentException(String.format("There is no registered crawler for the Sender \"%s\"", aSender.getName()));
        }
    }

    private void runCrawlers(final AbstractCrawler... aCrawlers)
    {
        try
        {
            final List<Future<Set<Film>>> results = executorService.invokeAll(Arrays.asList(aCrawlers));
            for (Future<Set<Film>> result : results)
            {
                filmlist.addAll(result.get());
            }
        } catch (ExecutionException | InterruptedException exception)
        {
            printMessage(ServerMessages.SERVER_ERROR);
            LOG.debug("Something went wrong while exeuting the crawlers.", exception);
        }
    }

    public void start()
    {
        TimeoutRunner timeoutRunner = new TimeoutRunner();
        if (config.getMaximumServerDurationInMinutes() != null && config.getMaximumServerDurationInMinutes() > 0)
        {
            Thread timeoutRunnerThread = new Thread(timeoutRunner);
            timeoutRunnerThread.start();
        }
        runCrawlers(crawlerMap.values().toArray(new AbstractCrawler[crawlerMap.size()]));
        timeoutRunner.stop();
    }

    private void printMessage(Message aMessage, Object... args)
    {
        messageListeners.parallelStream().forEach(l -> l.consumeMessage(aMessage, args));
    }

    public boolean addProgressListener(final CrawlerProgressListener aCrawlerProgressListener)
    {
        return progressListeners.add(aCrawlerProgressListener);
    }

    public boolean addMessageListener(final MessageListener aMessageListener)
    {
        return messageListeners.add(aMessageListener);
    }

    public boolean addAllProgressListener(final Collection<? extends CrawlerProgressListener> c)
    {
        return progressListeners.addAll(c);
    }

    public boolean addAllMessageListener(final Collection<? extends MessageListener> c)
    {
        return messageListeners.addAll(c);
    }

    public static void main(String... args)
    {
        List<ProgressLogMessageListener> progressListeners = new ArrayList<>();
        progressListeners.add(new ProgressLogMessageListener());

        List<MessageListener> messageListeners = new ArrayList<>();
        messageListeners.add(new LogMessageListener());

        final CrawlerManager manager = CrawlerManager.getInstance();
        manager.addAllProgressListener(progressListeners);
        manager.addAllMessageListener(messageListeners);
        manager.start();
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
                if (Duration.between(beginTime, LocalDateTime.now()).toMinutes() > config.getMaximumServerDurationInMinutes())
                {
                    forkJoinPool.shutdownNow();
                    printMessage(ServerMessages.SERVER_TIMEOUT);
                    stop();
                }
            }
        }
    }
}
