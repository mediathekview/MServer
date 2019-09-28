package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class ArteCrawler_EN extends ArteCrawler {

  public ArteCrawler_EN(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARTE_EN;
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.EN;
  }

  @Override
  protected boolean isDayEntriesEnabled() {
    return false;
  }
}
