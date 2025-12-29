package de.mediathekview.mserver.base.utils;


import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mserver.daten.Film;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.daten.Resolution;
import de.mediathekview.mserver.daten.Sender;
import de.mediathekview.mserver.base.utils.FileSizeDeterminer.ResponseInfo;


public class CheckUrlAvailability {
  private static final Logger LOG = LogManager.getLogger(CheckUrlAvailability.class);
  private final FileSizeDeterminer fsd;
  private int numberOfThreads = 10;
  private Long minFileSize = 2048L;
  private AtomicInteger removedCounter = new AtomicInteger(0);
  private long timeoutInMS = 1*60*1000L;
  private AtomicBoolean timeout = new AtomicBoolean(false);
  private long start = 0;
  
  public CheckUrlAvailability(final long minFileSize, final long timeoutInSec, final int numberOfThreads) {
    this.minFileSize = minFileSize;
    this.timeoutInMS = timeoutInSec*1000;
    this.numberOfThreads = numberOfThreads;
    fsd = new FileSizeDeterminer(30L, 30L, numberOfThreads);
  }
  
  public Filmlist getAvailableFilmlist(final Filmlist importList) {
    return getAvailableFilmlist(importList, true);
  }
  public Filmlist getAvailableFilmlist(final Filmlist importList, final boolean available) {
    LOG.debug("start getAvailableFilmlist(minSize {} byte, timeout {} sec)", this.minFileSize, (this.timeoutInMS/1000));
    start = System.currentTimeMillis();
    Filmlist filteredFilmlist = new Filmlist();
    filteredFilmlist.setCreationDate(importList.getCreationDate());
    filteredFilmlist.setListId(importList.getListId());
    //
    ForkJoinPool customThreadPool = new ForkJoinPool(numberOfThreads);
    customThreadPool.submit(() -> importList.getFilms().values().parallelStream()
            .filter(film -> isAvailable(film) == available)
            .forEach(filteredFilmlist::add))
            .join();
    customThreadPool.shutdown();
    customThreadPool.close();
    //
    LOG.debug("checked {} urls and removed {} in {} sec and timeout was reached: {}", importList.getFilms().size(), removedCounter.get(), ((System.currentTimeMillis()-start)/1000), timeout.get());
    return filteredFilmlist;
  }
  
  private boolean isAvailable(Film pFilm) {
    if (timeout.get() || System.currentTimeMillis() > (start+timeoutInMS)) {
      timeout.set(true);
      return true;
    }
    String normalUrl = pFilm.getDefaultUrl().get().getUrl().toString();
    ResponseInfo ri = fsd.getRequestInfo(normalUrl);

    if (pFilm.getThema().equalsIgnoreCase("Livestream")) {
      // do not remove livestreams
      return true;
    } else if (ri == null) {
      LOG.debug("Film response (null): {} # {} # {} # {} ", normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter.incrementAndGet();
      return false;
    } else if (!(ri.code() >= 200 && ri.code() < 300)) {
      LOG.debug("Film response ({}): {} # {} # {} # {} ", ri.code(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter.incrementAndGet();
      return false;
    } else if (ri.contentType().equalsIgnoreCase("text/html")) {
      LOG.debug("Film content type({}): {} # {} # {} # {} ", ri.contentType(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter.incrementAndGet();
      return false;
    } else if (ri.size() < minFileSize && !normalUrl.endsWith("m3u8")) {
      LOG.debug("Film small ({}): {} # {} # {} # {} ", ri.size() , normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter.incrementAndGet();
      return false;
    }  else if (removedVideo(pFilm, ri.path())) {
      LOG.debug("Film url ({}): {} # {} # {} # {} ", ri.path(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter.incrementAndGet();
      return false;
    }
    return true;
  }
  
  private boolean removedVideo(Film aFilm, String responseUrl) {
    return orfRemovedVideo(aFilm, responseUrl) || arteRemovedVideo(aFilm, responseUrl);
  }

  private boolean arteRemovedVideo(Film aFilm, String responseUrl) {
    if (aFilm.getSender().equals(Sender.ARTE_DE)) {
      return responseUrl.contains("_EXTRAIT_");
    }
    return false;
  }

  private boolean orfRemovedVideo(Film aFilm, String responseUrl) {
    if (aFilm.getSender().equals(Sender.ORF)) {
      return responseUrl.toLowerCase().contains("/bearbeitung_") || 
          responseUrl.toLowerCase().contains("/geoprotection");
    }
    return false;
  }
}
