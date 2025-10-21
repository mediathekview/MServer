package de.mediathekview.mserver.base.progress;
/**
 * A abstract listener for listeners which get progress updates.
 */
public interface ProgressListener
{
    void updateProgess(Progress aCrawlerProgress);
}
