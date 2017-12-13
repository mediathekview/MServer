package de.mediathekview.mserver.crawler.hr;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.hr.tasks.HrSendungenOverviewPageTask;
import de.mediathekview.mserver.crawler.hr.tasks.HrSendungsfolgenOverviewPageTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class HrCrawler extends AbstractCrawler {
  public static final String BASE_URL = "http://www.hr-fernsehen.de/";

  public HrCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager aRootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, aRootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.HR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final HrSendungenOverviewPageTask sendungenOverviewPageTask = new HrSendungenOverviewPageTask();

    try {
      final HrSendungsfolgenOverviewPageTask sendungsfolgenOverviewPageTask =
          new HrSendungsfolgenOverviewPageTask(this,
              new ConcurrentLinkedQueue<>(forkJoinPool.submit(sendungenOverviewPageTask).get()));
    } catch (InterruptedException | ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

}
