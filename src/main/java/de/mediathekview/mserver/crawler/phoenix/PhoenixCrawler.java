package de.mediathekview.mserver.crawler.phoenix;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PhoenixCrawler extends AbstractCrawler {

  private static final Logger LOG = LogManager.getLogger(PhoenixCrawler.class);

  public PhoenixCrawler(ForkJoinPool aForkJoinPool,
      Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners,
      MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.PHOENIX;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    LOG.fatal("Not implemented");
    return null;
  }
}
