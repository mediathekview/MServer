package de.mediathekview.mserver.crawler.ard;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.utils.DateUtils;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.ard.tasks.*;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

public class ArdCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ArdCrawler.class);

  public ArdCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARD;
  }

  private Queue<CrawlerUrlDTO> createDayUrlsToCrawl() {
    final Queue<CrawlerUrlDTO> dayUrlsToCrawl = new ConcurrentLinkedQueue<>();
    final List<String> days = DateUtils.generateDaysToCrawl(crawlerConfig);
    days.forEach( dateString -> {
      for (final String client : ArdConstants.CLIENTS) {
        final String url = String.format(ArdConstants.DAY_PAGE_URL, dateString, client);
        dayUrlsToCrawl.offer(new CrawlerUrlDTO(url));
      }
    });
    return dayUrlsToCrawl;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    ConcurrentLinkedQueue<CrawlerUrlDTO> test = new ConcurrentLinkedQueue<>();
    try {
      final ForkJoinTask<Set<ArdFilmInfoDto>> dayTask =
          forkJoinPool.submit(new ArdDayPageTask(this, createDayUrlsToCrawl()));

      final Set<ArdFilmInfoDto> shows = dayTask.get();
      shows.clear();
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      if (Boolean.TRUE.equals(crawlerConfig.getTopicsSearchEnabled())) {
        final Set<ForkJoinTask<Set<CrawlerUrlDTO>>> senderTopicTasks = createSenderTopicTasks();

        final Set<CrawlerUrlDTO> senderTopicUrls = new HashSet<>();
        for (final ForkJoinTask<Set<CrawlerUrlDTO>> senderTopicTask : senderTopicTasks) {
          senderTopicUrls.addAll(senderTopicTask.get());
        }
        LOG.debug("sender topic tasks: {}", senderTopicUrls.size());
        final ArdTopicGroupsTask groupsToAsset = new ArdTopicGroupsTask(this, new ConcurrentLinkedQueue<>(senderTopicUrls));
        final Set<CrawlerUrlDTO> assitUrls = new HashSet<>();
        assitUrls.addAll(forkJoinPool.submit(groupsToAsset).get());
        LOG.debug("sender group assit tasks: {}", assitUrls.size());
        
        //test.add(new CrawlerUrlDTO("https://api.ardmediathek.de/page-gateway/widgets/swr/asset/Y3JpZDovL3N3ci5kZS8yNDEwMzY1MA?pageNumber=0&pageSize=48&embedded=true&seasoned=false&seasonNumber=&withAudiodescription=false&withOriginalWithSubtitle=false&withOriginalversion=false&single=false"));
        test.add(new CrawlerUrlDTO("https://api.ardmediathek.de/page-gateway/widgets/wdr/asset/Y3JpZDovL3dkci5kZS93ZXN0cG9s?pageNumber=0&pageSize=48&embedded=true&seasoned=false&seasonNumber=&withAudiodescription=false&withOriginalWithSubtitle=false&withOriginalversion=false&single=false"));
        
        final ArdTopicPageTask topicTask =
            new ArdTopicPageTask(this, new ConcurrentLinkedQueue<>(assitUrls));
            //new ArdTopicPageTask(this, new ConcurrentLinkedQueue<>(test));
              
        final int showsCountBefore = shows.size();
        shows.addAll(forkJoinPool.submit(topicTask).get());
        LOG.debug(
            "ARD crawler found {} topics for all sub-sender.", shows.size() - showsCountBefore);
      }
      //
      final Queue<ArdFilmInfoDto> showsFiltered = this.filterExistingFilms(shows, ArdFilmInfoDto::getId);
      //
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), showsFiltered.size());
      getAndSetMaxCount(showsFiltered.size());
      return new ArdFilmDetailTask(this, new ConcurrentLinkedQueue<>(showsFiltered));
    } catch (final InterruptedException ex) {
      LOG.fatal("Exception in ARD crawler.", ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in ARD crawler.", ex);
    }
    return null;
  }

  private Set<ForkJoinTask<Set<CrawlerUrlDTO>>> createSenderTopicTasks() {
    final Set<ForkJoinTask<Set<CrawlerUrlDTO>>> topicTasks = new HashSet<>();
    try {
      topicTasks.add(getTopicEntriesBySender(ArdConstants.DEFAULT_CLIENT));
    } catch (ExecutionException | InterruptedException e) {
      LOG.error("exception sender topic {}", ArdConstants.DEFAULT_CLIENT, e);
    }
    for (final String client : ArdConstants.CLIENTS) {
      try {
        topicTasks.add(getTopicEntriesBySender(client));
      } catch (ExecutionException | InterruptedException e) {
        LOG.error("exception sender topic {}", client, e);
      }
    }
    return topicTasks;
  }

  private ForkJoinTask<Set<CrawlerUrlDTO>> getTopicEntriesBySender(final String sender) throws ExecutionException, InterruptedException {
     Set<CrawlerUrlDTO> senderSingleLetterUrls = forkJoinPool.submit(
        new ArdTopicsTask(this, sender, CreateLetterUrlQuery(sender))).get();

     LOG.debug("topics task result {}", senderSingleLetterUrls.size());
     return forkJoinPool.submit(new ArdTopicsLetterTask(this, sender, new ConcurrentLinkedQueue<>(senderSingleLetterUrls)));
  }

  private Queue<CrawlerUrlDTO> CreateLetterUrlQuery(final String client) {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    final String url = String.format(ArdConstants.TOPICS_URL, client);
    urls.add(new CrawlerUrlDTO(url));

    return urls;
  }
}