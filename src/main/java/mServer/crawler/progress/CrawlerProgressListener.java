package mServer.crawler.progress;

/**
 * A abstract listener for listeners which get crawler progress updates.
 */
public interface CrawlerProgressListener
{
    void updateCrawlerProgess(CrawlerProgress aCrawlerProgress);
}
