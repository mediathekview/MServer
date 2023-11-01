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
  private long timeoutInMS = 1*60*1000;
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
    importList.getFilms().values().stream().parallel().filter(e -> { return isAvailable(e);}).forEach(filteredFilmlist::add);
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

    
    // ignore m3u8 ? Why?
    if (!normalUrl.endsWith("m3u8")) {
      if (ri.getCode() != 200) {
        LOG.info("Film response ({}): {} # {} # {} # {} ", ri.getCode(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
        removedCounter++;
        return false;
      } else if (ri.getSize() < minFileSize) {
        LOG.info("Film to small ({}): {} # {} # {} # {} ", ri.getSize() , normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
        removedCounter++;
        return false;
      } else if (ri.getContentType().equalsIgnoreCase("text/html")) {
        LOG.info("Film response type({}): {} # {} # {} # {} ", ri.getContentType(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
        removedCounter++;
        return false;
      } else if (removedVideo(pFilm, ri.getPath())) {
        LOG.info("Film url ({}): {} # {} # {} # {} ", ri.getPath(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
        removedCounter++;
        return false;
      }
    } else {
      if (ri.getCode() != 200) {
        LOG.info("M3U8 response({}): {} # {} # {} # {} ", ri.getCode(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      } else if (ri.getContentType().equalsIgnoreCase("text/html")) {
        LOG.info("M3U8 response type({}): {} # {} # {} # {} ", ri.getCode(), normalUrl, pFilm.getSender(), pFilm.getThema(), pFilm.getTitel());
      }
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
