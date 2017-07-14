package de.mediathekview.mserver.base.progress;
import de.mediathekview.mlib.daten.Sender;
/**
 * A abstract listener for listeners which get crawler progress updates.
 */
public interface CrawlerProgressListener
{
    void updateCrawlerProgess(Sender aSender, CrawlerProgress aCrawlerProgress);
}
