package de.mediathekview.mserver.progress.listeners;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.progress.Progress;

/**
 * A abstract Sender specific listener for listeners which get progress updates.
 */
public interface SenderProgressListener
{
    void updateProgess(Sender aSender, Progress aCrawlerProgress);
}
