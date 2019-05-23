package de.mediathekview.mserver.crawler.dreisat;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.zdf.AbstractZdfCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class DreiSatCrawler extends AbstractZdfCrawler {

  public DreiSatCrawler(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  protected @NotNull String getUrlBase() {
    return DreisatConstants.URL_BASE;
  }

  @Override
  protected String getApiUrlBase() {
    return DreisatConstants.URL_API_BASE;
  }

  @Override
  protected @NotNull String getUrlDay() {
    return DreisatConstants.URL_DAY;
  }

  @Override
  public Sender getSender() {
    return Sender.DREISAT;
  }
}
