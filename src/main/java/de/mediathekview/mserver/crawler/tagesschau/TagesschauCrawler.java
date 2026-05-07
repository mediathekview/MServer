package de.mediathekview.mserver.crawler.tagesschau;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.base.messages.ServerMessages;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.crawler.basic.CrawlerUrlDTO;
import de.mediathekview.mserver.crawler.tagesschau.tasks.TagesschauFilmTask;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

/**
 * Crawler for the Tagesschau "vor 20 Jahren" (20 years ago) archive.
 * Extracts daily news broadcasts from the archive.
 */
public class TagesschauCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(TagesschauCrawler.class);

  public TagesschauCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.TAGESSCHAU24;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    try {
      // Create URLs for the last YEARS_BACK years
      Queue<CrawlerUrlDTO> filmUrls = createFilmUrls();

      if (filmUrls.isEmpty()) {
        LOG.warn("No URLs created for Tagesschau crawler");
        return null;
      }

      // Set max count for progress tracking
      getAndSetMaxCount(filmUrls.size());

      printMessage(
          ServerMessages.DEBUG_ALL_SENDUNG_FOLGEN_COUNT,
          getSender().getName(),
          filmUrls.size());

      // Return the task that will process the URLs
      return new TagesschauFilmTask(this, filmUrls);

    } catch (final Exception ex) {
      LOG.fatal("Exception in Tagesschau crawler.", ex);
      printErrorMessage();
    }
    return null;
  }

  /**
   * Creates URLs for the daily broadcast pages.
   * We need to crawl through the years and generate URLs for each day.
   */
  private Queue<CrawlerUrlDTO> createFilmUrls() {
    Queue<CrawlerUrlDTO> urls = new ConcurrentLinkedQueue<>();

    try {
      // For now, we start by fetching the main archive page
      // This page contains links to the individual days
      urls.add(new CrawlerUrlDTO(TagesschauConstants.ARCHIVE_START_URL));

    } catch (final Exception e) {
      LOG.error("Error creating film URLs", e);
    }

    return urls;
  }
}


