package de.mediathekview.mserver.crawler.ard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.CategoriesAZ;
import de.mediathekview.mserver.crawler.ard.tasks.ArdSendungTask;
import de.mediathekview.mserver.crawler.ard.tasks.ArdSendungenOverviewPageCrawler;
import de.mediathekview.mserver.crawler.ard.tasks.ArdSendungsfolgenOverviewPageCrawler;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class ArdCrawler extends AbstractCrawler {
  public static final String ARD_BASE_URL = "http://www.ardmediathek.de";
  private static final String ARD_CATEGORY_BASE_URL =
      ARD_BASE_URL + "/tv/sendungen-a-z?buchstabe=%s";
  private static final String ARD_DAY_BASE_URL = ARD_BASE_URL + "/tv/sendungVerpasst?tag=%d";

  public ArdCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners);
  }

  @Override
  public Sender getSender() {
    return Sender.ARD;
  }

  private RecursiveTask<Set<ArdSendungBasicInformation>> createCategoriesOverviewPageCrawler() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> categoryUrlsToCrawl = new ConcurrentLinkedQueue<>();
    Arrays.stream(CategoriesAZ.values())
        .map(c -> new CrawlerUrlDTO(String.format(ARD_CATEGORY_BASE_URL, c.getKey())))
        .forEach(categoryUrlsToCrawl::offer);
    return new ArdSendungenOverviewPageCrawler(this, categoryUrlsToCrawl);
  }

  private RecursiveTask<Set<ArdSendungBasicInformation>> createDaysOverviewPageCrawler() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < config.getMaximumDaysForSendungVerpasstSection(); i++) {
      dayUrlsToCrawl.offer(new CrawlerUrlDTO(String.format(ARD_DAY_BASE_URL, i)));
    }
    return new ArdSendungsfolgenOverviewPageCrawler(this, dayUrlsToCrawl);
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final RecursiveTask<Set<ArdSendungBasicInformation>> categoriesTask =
        createCategoriesOverviewPageCrawler();
    final RecursiveTask<Set<ArdSendungBasicInformation>> daysTask = createDaysOverviewPageCrawler();
    forkJoinPool.execute(categoriesTask);
    forkJoinPool.execute(daysTask);

    final ConcurrentLinkedQueue<ArdSendungBasicInformation> ardSendungBasicInformation =
        new ConcurrentLinkedQueue<>();
    ardSendungBasicInformation.addAll(categoriesTask.join());
    ardSendungBasicInformation.addAll(daysTask.join());

    return new ArdSendungTask(this, ardSendungBasicInformation);
  }
}
