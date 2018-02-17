/*
 * SwrCrawler.java
 * 
 * Projekt    : MServer
 * erstellt am: 11.02.2018
 * Autor      : Sascha
 * 
 */
package de.mediathekview.mserver.crawler.swr;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.messages.listener.MessageListener;
import de.mediathekview.mserver.base.config.MServerConfigManager;
import de.mediathekview.mserver.crawler.basic.AbstractCrawler;
import de.mediathekview.mserver.progress.listeners.SenderProgressListener;

public class SwrCrawler extends AbstractCrawler {
  
  private static final Logger LOG = LogManager.getLogger(SwrCrawler.class);
  public static final String BASE_URL = "https://swrmediathek.de/";
  private static final String SENDUNG_VERPASST_URL_TEMPLATE =
      BASE_URL + "sendungverpasst.htm?show=&date=%s";
  private static final DateTimeFormatter URL_DATE_TIME_FORMATTER = DateTimeFormatter.BASIC_ISO_DATE;  

  public SwrCrawler(ForkJoinPool aForkJoinPool, Collection<MessageListener> aMessageListeners,
      Collection<SenderProgressListener> aProgressListeners, MServerConfigManager rootConfig) {
    super(aForkJoinPool, aMessageListeners, aProgressListeners, rootConfig);
  }

  @Override
  public Sender getSender() {
    return Sender.SWR;
  }

  @Override
  protected RecursiveTask<Set<Film>> createCrawlerTask() {
    
    
    // TODO Auto-generated method stub
    return null;
  }

}
