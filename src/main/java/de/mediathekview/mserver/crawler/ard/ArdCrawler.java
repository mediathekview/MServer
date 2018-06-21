package de.mediathekview.mserver.crawler.ard;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveTask;
import java.util.stream.Collectors;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.CategoriesAZ;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.ard.tasks.ArdGetKanaeleTask;
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
  private static final String ARD_DAY_KANAL_BASE_URL =
      ARD_BASE_URL + "/tv/sendungVerpasst?tag=%d&kanal=%s";

  public ArdCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARD;
  }

  private RecursiveTask<Set<ArdSendungBasicInformation>> createCategoriesOverviewPageCrawler() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> categoryUrlsToCrawl = new ConcurrentLinkedQueue<>();
    Arrays.stream(CategoriesAZ.values())
        .map(category -> new CrawlerUrlDTO(String.format(ARD_CATEGORY_BASE_URL, category.getKey())))
        .forEach(categoryUrlsToCrawl::offer);
    return new ArdSendungenOverviewPageCrawler(this, categoryUrlsToCrawl);
  }

  private RecursiveTask<Set<ArdSendungBasicInformation>> createDaysOverviewPageCrawler(
      final Set<String> kanaele) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
    dayUrlsToCrawl.addAll(createDayUrlsToCrawl(Optional.empty()));
    dayUrlsToCrawl.addAll(kanaele.stream().map(Optional::of).map(this::createDayUrlsToCrawl)
        .flatMap(ConcurrentLinkedQueue::stream).collect(Collectors.toSet()));
    return new ArdSendungsfolgenOverviewPageCrawler(this, dayUrlsToCrawl);
  }

  private ConcurrentLinkedQueue<CrawlerUrlDTO> createDayUrlsToCrawl(final Optional<String> kanal) {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();

    for (int i = 0; i < crawlerConfig.getMaximumDaysForSendungVerpasstSection(); i++) {
      if (kanal.isPresent()) {
        dayUrlsToCrawl.offer(new CrawlerUrlDTO(String.format(ARD_DAY_KANAL_BASE_URL, i, kanal)));
      } else {
        dayUrlsToCrawl.offer(new CrawlerUrlDTO(String.format(ARD_DAY_BASE_URL, i)));
      }
    }
    return dayUrlsToCrawl;

  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final Future<Set<String>> kanaele =
        forkJoinPool.submit(new ArdGetKanaeleTask(this, String.format(ARD_DAY_BASE_URL, 0)));

    final RecursiveTask<Set<ArdSendungBasicInformation>> categoriesTask =
        createCategoriesOverviewPageCrawler();
    final RecursiveTask<Set<ArdSendungBasicInformation>> daysTask =
        createDaysOverviewPageCrawler(kanaele.get());
    forkJoinPool.execute(categoriesTask);
    forkJoinPool.execute(daysTask);

    final ConcurrentLinkedQueue<ArdSendungBasicInformation> ardSendungBasicInformation =
        new ConcurrentLinkedQueue<>();
    ardSendungBasicInformation.addAll(categoriesTask.join());
    ardSendungBasicInformation.addAll(daysTask.join());

    return new ArdSendungTask(this, ardSendungBasicInformation);
  }
}
