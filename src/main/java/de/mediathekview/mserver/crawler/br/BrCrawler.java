package de.mediathekview.mserver.crawler.br;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.br.data.BrID;
import de.mediathekview.mserver.crawler.br.tasks.BrBroadcastsTask;
import de.mediathekview.mserver.crawler.br.tasks.BrGetClipDetailsTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class BrCrawler extends AbstractCrawler {
  private static final Logger LOG = LogManager.getLogger(BrCrawler.class);
  public static final String BASE_URL = "https://www.br.de/mediathek/";

  public BrCrawler(
      final ForkJoinPool aForkJoinPool,
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

    final RecursiveTask<Set<BrID>> createCompleteClipListTask = createGetClipListCrawler();
    ConcurrentLinkedQueue<BrID> idList = null;

    try {
      final Set<BrID> completeClipList = forkJoinPool.submit(createCompleteClipListTask).get();

      idList = new ConcurrentLinkedQueue<>(completeClipList);
      incrementMaxCountBySizeAndGetNewSize(idList.size());
      printMessage(
          ServerMessages.DEBUG_MSSING_SENDUNGFOLGEN_COUNT, getSender().getName(), idList.size());
    } catch (final InterruptedException | ExecutionException exception) {
      LOG.fatal("Something went terrible wrong collecting the clip details");
      exception.printStackTrace();
      printErrorMessage();
    }

    return new BrGetClipDetailsTask(this, idList);
  }

  private RecursiveTask<Set<BrID>> createGetClipListCrawler() {
    final LocalDate now = LocalDate.now();
    final Queue<BrQueryDto> input = new ConcurrentLinkedQueue<>();

    for (int i = 0; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      final LocalDate day = now.minusDays(i);
      BrQueryDto dto = new BrQueryDto(BrConstants.GRAPHQL_API, day, day, BrConstants.PAGE_SIZE, Optional.empty());
      input.add(dto);
    }
    for (int i = 1; i <= crawlerConfig.getMaximumDaysForSendungVerpasstSectionFuture(); i++) {
      final LocalDate day = now.plusDays(i);
      BrQueryDto dto = new BrQueryDto(BrConstants.GRAPHQL_API, day, day, BrConstants.PAGE_SIZE, Optional.empty());
      input.add(dto);
    }
    return new BrBroadcastsTask(this, input);
  }
}
