package de.mediathekview.mserver.crawler.funk;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.funk.parser.FunkOverviewDTO;
import de.mediathekview.mserver.crawler.funk.tasks.FunkOverviewTask;
import de.mediathekview.mserver.crawler.funk.tasks.FunkVideoTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class FunkCrawler extends AbstractCrawler {
  private static final String AUTH_KEY =
      "1efb06afc842521f5693b5ce4e5b6c4530ce4ea8c1c09ed618f91da39c11da92";

  public FunkCrawler(final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners);
  }

  @Override
  public Sender getSender() {
    return Sender.FUNK;
  }

  private void addUrls(final ConcurrentLinkedQueue<CrawlerUrlDTO> aUrlsToCrawl) {
    aUrlsToCrawl.offer(new CrawlerUrlDTO("https://api.funk.net/v1.1/content/"));
    aUrlsToCrawl.offer(new CrawlerUrlDTO("https://api.funk.net/v1.1/content/formats/"));
    aUrlsToCrawl.offer(new CrawlerUrlDTO("https://api.funk.net/v1.1/content/series/"));

  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    final ConcurrentLinkedQueue<CrawlerUrlDTO> urlsToCrawl = new ConcurrentLinkedQueue<>();
    addUrls(urlsToCrawl);
    final FunkOverviewTask overviewTask =
        new FunkOverviewTask(this, urlsToCrawl, false, Optional.of(AUTH_KEY));
    final Set<FunkOverviewDTO> overviewResults = overviewTask.invoke();

    final ConcurrentLinkedQueue<CrawlerUrlDTO> folgenUrls = new ConcurrentLinkedQueue<>();
    overviewResults.parallelStream().map(FunkOverviewDTO::getUrls).forEach(folgenUrls::addAll);

    // TODO Create new own task to add Format/Serie infos to DTO.
    final FunkOverviewTask folgenTask =
        new FunkOverviewTask(this, folgenUrls, true, Optional.of(AUTH_KEY));
    final Set<FunkOverviewDTO> folgenResults = folgenTask.invoke();

    final ConcurrentLinkedQueue<CrawlerUrlDTO> videoUrls = new ConcurrentLinkedQueue<>();
    folgenResults.parallelStream().map(FunkOverviewDTO::getUrls).forEach(videoUrls::addAll);

    return new FunkVideoTask(this, urlsToCrawl, Optional.of(AUTH_KEY));
  }

}
