package de.mediathekview.mserver.crawler.br;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.tasks.BrAllSendungenTask;
import de.mediathekview.mserver.crawler.br.tasks.BrMissedSendungsFolgenTask;
import de.mediathekview.mserver.crawler.br.tasks.BrSendungDetailsTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class BrCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(BrCrawler.class);
  public static final String BASE_URL = "https://beta.mediathek.br.de";

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
    return new BrAllSendungenTask(this, forkJoinPool);
  }

  private Callable<Set<String>> createMissedFilmsCrawler() {
    return new BrMissedSendungsFolgenTask(this);
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
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong on gatherin the missed Films");
      printErrorMessage();
    }
    brFilmIds.addAll(sendungenFilmsTask.join());

    return new BrSendungDetailsTask(this, brFilmIds);
  }

}
