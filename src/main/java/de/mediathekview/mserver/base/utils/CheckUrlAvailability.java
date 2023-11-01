package de.mediathekview.mserver.base.utils;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.daten.Sender;
import de.mediathekview.mlib.tool.FileSizeDeterminer;
import de.mediathekview.mlib.tool.FileSizeDeterminer.RespoonseInfo;


public class CheckUrlAvailability {
  private static final Logger LOG = LogManager.getLogger(CheckUrlAvailability.class);
  private Long minFileSize = 2048L;
  private int removedCounter = 0;
  private long timeoutInMS = 1*60*1000L;
  private boolean timeout = false;
  private long start = 0;
  private FileSizeDeterminer fsd = new FileSizeDeterminer();

  public CheckUrlAvailability(long minFileSize, long timeoutInSec) {
    this.minFileSize = minFileSize;
    this.timeoutInMS = timeoutInSec*1000;
  }
  
  public Filmlist getAvaiableFilmlist(final Filmlist importList) {
    LOG.debug("start getAvaiableFilmlist(minSize {} byte, timeout {} sec)", this.minFileSize, (this.timeoutInMS/1000));
    start = System.currentTimeMillis();
    Filmlist filteredFilmlist = new Filmlist();
    filteredFilmlist.setCreationDate(importList.getCreationDate());
    filteredFilmlist.setListId(importList.getListId());
    importList.getFilms().values().stream().parallel().filter(this::isAvailable).forEach(filteredFilmlist::add);
    LOG.debug("checked {} urls and removed {} in {} sec and timeout was reached: {}", importList.getFilms().size(), removedCounter, ((System.currentTimeMillis()-start)/1000), timeout);
    return filteredFilmlist;
  }
  
  private boolean isAvailable(Film pFilm) {
    if (timeout || System.currentTimeMillis() > (start+timeoutInMS)) {
      timeout = true;
      return true;
    }
    
    String normalUrl = pFilm.getUrl(Resolution.NORMAL).getUrl().toString();
    RespoonseInfo ri = fsd.getRequestInfo(normalUrl);

    if (pFilm.getThema().equalsIgnoreCase("Livestream")) {
      // do not remove livestreams
      return true;
    } else if (ri.getCode() == 404) {
      LOG.debug("Film response ({}): {} # {} # {} # {} ", ri.getCode(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter++;
      return false;
    } else if (ri.getContentType().equalsIgnoreCase("text/html")) {
      LOG.debug("Film content type({}): {} # {} # {} # {} ", ri.getContentType(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter++;
      return false;
    } else if (ri.getSize() < minFileSize && !normalUrl.endsWith("m3u8")) {
      LOG.debug("Film small ({}): {} # {} # {} # {} ", ri.getSize() , normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter++;
      return false;
    }  else if (removedVideo(pFilm, ri.getPath())) {
      LOG.debug("Film url ({}): {} # {} # {} # {} ", ri.getPath(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      removedCounter++;
      return false;
    }
    // just for debugging
    if (ri.getCode() != 200) {
      LOG.debug("Film not removed but status!=200 ({}): {} # {} # {} # {} ", ri.getCode(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
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
