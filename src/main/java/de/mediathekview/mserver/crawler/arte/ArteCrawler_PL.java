package de.mediathekview.mserver.crawler.arte;

import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

import java.util.Collection;
import java.util.concurrent.ForkJoinPool;

public class ArteCrawler_PL extends ArteCrawler {

  public ArteCrawler_PL(
      final ForkJoinPool aForkJoinPool,
      final Collection<MessageListener> aMessageListeners,
      final Collection<SenderProgressListener> aProgressListeners,
      final MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.ARTE_PL;
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.PL;
  }

  @Override
  protected boolean isDayEntriesEnabled() {
    return false;
  }
}
