package de.mediathekview.mserver.crawler.br;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.tasks.BrGetClipIDsTask;
import de.mediathekview.mserver.crawler.br.tasks.BrGetClipDetailsTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class BrCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(BrCrawler.class);
  public static final String BASE_URL = "https://www.br.de/mediathek/";

  public BrCrawler(final ForkJoinPool aForkJoinPool,
          final Collection<MessageListener> aMessageListeners,
          final Collection<SenderProgressListener> aProgressListeners,
          final MServerConfigManager rootConfig) {
      super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.BR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    
    final Callable<Set<BrID>> createCompleteClipListTask = createGetClipListCrawler();
    ConcurrentLinkedQueue<BrID> idList = null;
    
    try {
      final Set<BrID> completeClipList = forkJoinPool.submit(createCompleteClipListTask).get();
      
      idList = new ConcurrentLinkedQueue<>(completeClipList);
      printMessage(ServerMessages.DEBUG_MSSING_SENDUNGFOLGEN_COUNT, getSender().getName(), idList.size());
    } catch (InterruptedException | ExecutionException exception) {
      LOG.fatal("Something wen't terrible wrong collecting the clip details");
      printErrorMessage();
    }
    
    return new BrGetClipDetailsTask(this, idList);
  }

  private Callable<Set<BrID>> createGetClipListCrawler() {
    return new BrGetClipIDsTask(this);
  }


}
