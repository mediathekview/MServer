package de.mediathekview.mserver.crawler.dreisat;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class DreiSatCrawler extends AbstractCrawler {

  public DreiSatCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners);
  }

  @Override
  public Sender getSender() {
    return Sender.DREISAT;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    // TODO Auto-generated method stub
    return null;
  }

}
