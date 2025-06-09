package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfFilmTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfLetterPageTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfPubFormTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTopicSeasonTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ZdfCrawler.class);
  private static final int MAX_LETTER_PAGEGS = 27;

  public ZdfCrawler(
      ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ZDF;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {

    final String authKey = "aa3noh4ohz9eeboo8shiesheec9ciequ9Quah7el";
    try {
      final Set<ZdfFilmDto> shows = new HashSet<>();

      ZdfLetterPageTask letterPageTask =
          new ZdfLetterPageTask(this, createLetterPageUrls(), authKey);
      final Set<ZdfTopicUrlDto> topicUrls = forkJoinPool.submit(letterPageTask).get();

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), topicUrls.size());

      final ZdfPubFormTask pubFormTask = new ZdfPubFormTask(this, createPubFormUrls(), authKey);
      final Set<ZdfPubFormResult> pubFormUrls = forkJoinPool.submit(pubFormTask).get();

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName() + " - PubForm:", pubFormUrls.size());

      pubFormUrls.forEach(
          pubFormResult -> {
            topicUrls.addAll(pubFormResult.getTopics().getElements());
            shows.addAll(pubFormResult.getFilms());
          });
      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName() + " - PubForm-Topics integrated: ", topicUrls.size());

      ZdfTopicSeasonTask topicSeasonTask =
          new ZdfTopicSeasonTask(this, new ConcurrentLinkedQueue<>(topicUrls), authKey);
      shows.addAll(forkJoinPool.submit(topicSeasonTask).get());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), shows.size());

      return new ZdfFilmTask(this, new ConcurrentLinkedQueue<>(shows), authKey);
    } catch (final InterruptedException ex) {
      LOG.debug("{} crawler interrupted.", getSender().getName(), ex);
      Thread.currentThread().interrupt();
    } catch (final ExecutionException ex) {
      LOG.fatal("Exception in {} crawler.", getSender().getName(), ex);
    }
    return null;
  }

  private Queue<ZdfPubFormDto> createPubFormUrls() {
    Queue<ZdfPubFormDto> urls = new ConcurrentLinkedQueue<>();
    ZdfConstants.SPECIAL_COLLECTION_IDS.forEach((collectionId, topic) -> {
      final String url =
          ZdfUrlBuilder.buildTopicNoSeasonUrl(
              ZdfConstants.EPISODES_PAGE_SIZE, collectionId, ZdfConstants.NO_CURSOR);
      urls.add(new ZdfPubFormDto(topic, collectionId, url));
    });
    return urls;
  }

  private Queue<ZdfLetterDto> createLetterPageUrls() {
    final Queue<ZdfLetterDto> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < MAX_LETTER_PAGEGS; i++) {
      urls.add(new ZdfLetterDto(i, ZdfUrlBuilder.buildLetterPageUrl(ZdfConstants.NO_CURSOR, i)));
    }

    return urls;
  }
}
