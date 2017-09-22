package de.mediathekview.mserver.crawler.br;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.tasks.BrAllSendungenTask;
import de.mediathekview.mserver.crawler.br.tasks.BrSendungDetailsTask;
import de.mediathekview.mserver.crawler.br.tasks.BrSendungenMissedTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class BrCrawler extends AbstractCrawler {
  private static final String BASE_URL = "https://beta.mediathek.br.de/";

  public BrCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners);
  }

  @Override
  public Sender getSender() {
    return Sender.BR;
  }

  private RecursiveTask<Set<String>> createAllSendungenOverviewCrawler() {
    return new BrAllSendungenTask(this);
  }

  private Callable<Set<String>> createMissedFilmsCrawler() {
    return new BrSendungenMissedTask(this);
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Callable<Set<String>> missedFilmsTask = createMissedFilmsCrawler();
    final RecursiveTask<Set<String>> sendungenFilmsTask = createAllSendungenOverviewCrawler();
    final Future<Set<String>> missedFilmIds = forkJoinPool.submit(missedFilmsTask);
    forkJoinPool.execute(sendungenFilmsTask);

    final ConcurrentLinkedQueue<String> brFilmIds = new ConcurrentLinkedQueue<>();
    try {
      brFilmIds.addAll(missedFilmIds.get());
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (ExecutionException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    brFilmIds.addAll(sendungenFilmsTask.join());

    return new BrSendungDetailsTask(this, brFilmIds);
  }

}
