package de.mediathekview.mserver.crawler.zdf;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.basic.TopicUrlDTO;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfFilmTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfLetterPageTask;
import de.mediathekview.mserver.crawler.zdf.tasks.ZdfTopicSeasonTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ZdfCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(ZdfCrawler.class);
  private static final int MAX_LETTER_PAGEGS = 27;

  public ZdfCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners, Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
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
      ZdfLetterPageTask letterPageTask = new ZdfLetterPageTask(this, createLetterPageUrls(), authKey);
      final Set<TopicUrlDTO> topicUrls = forkJoinPool.submit(letterPageTask).get();

      printMessage(
              ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT, getSender().getName(), topicUrls.size());

      ZdfTopicSeasonTask topicSeasonTask =
          new ZdfTopicSeasonTask(this, new ConcurrentLinkedQueue<>(topicUrls), authKey);
      final Set<ZdfFilmDto> shows = forkJoinPool.submit(topicSeasonTask).get();

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

  private Queue<CrawlerUrlDTO> createLetterPageUrls() {
    final Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();
    for (int i = 0; i < MAX_LETTER_PAGEGS; i++) {
      urls.add(new CrawlerUrlDTO(ZdfUrlBuilder.buildLetterPageUrl(i)));
    }

    return urls;
  }
}
