package de.mediathekview.mserver.ui.config;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import de.mediathekview.mserver.base.utils.CheckUrlAvailability;
import de.mediathekview.mserver.base.utils.FilmDBService;
import de.mediathekview.mserver.crawler.CrawlerManager;
import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.ui.config.MServerCommandLine.CMDARG;

public class MServerExecutionFlow {
  private static final Logger LOG = LogManager.getLogger(MServerExecutionFlow.class);
  private CrawlerManager manager;
  private Map<CMDARG,String> cmd;
  
  public MServerExecutionFlow(CrawlerManager manager, Map<CMDARG,String> cmd) {
    this.manager = manager;
    this.cmd = cmd;
  }
  
  void start() {
    if(cmd.containsKey(CMDARG.topicsSearchEnabled)) {
      if(cmd.get(CMDARG.topicsSearchEnabled).equalsIgnoreCase("true")) {
        manager.getConfigManager().getConfig().setTopicsSearchEnabled(true);
      } else {
        manager.getConfigManager().getConfig().setTopicsSearchEnabled(false);
      }
    }
    //
    if (cmd.containsKey(CMDARG.flow) && cmd.get(CMDARG.flow).equalsIgnoreCase("importFilmlistIntoDB")) {
      importFilmlistIntoDB();
    } else if (cmd.containsKey(CMDARG.flow) && cmd.get(CMDARG.flow).equalsIgnoreCase("exportFilmListFromDB")) {
      exportFilmListFromDB();
    } else if (cmd.containsKey(CMDARG.flow) && cmd.get(CMDARG.flow).equalsIgnoreCase("checkAvailability")) {
      checkAvailability();
    } else {
      startCrawlerFlow();
    }
  }
  
  void startCrawlerFlow() {
    try {
      manager.start();
      manager.filterFilmlist();
      manager.storeFilmsToDB();
      manager.importFilmlist();
      manager.importLivestreamFilmlist();
    } finally {
      manager.filterFilmlist();
      manager.saveFilmlist();
      manager.saveDifferenceFilmlist();
      manager.writeHashFile();
      manager.writeIdFile();
      manager.copyFilmlist();
      manager.stop();
    }
  }
  
  void exportFilmListFromDB() {
    try {
      FilmDBService filmDBService = new FilmDBService(manager.getExecutorService(), 2000);
      Optional<Filmlist> dbFilmlist = filmDBService.readFilmlistFromDB();
      dbFilmlist.ifPresent(filmlist -> manager.getFilmlist().addAllFilms(filmlist.getFilms().values()));
      //
      manager.importLivestreamFilmlist();
    } finally {
      manager.filterFilmlist();
      manager.saveFilmlist();
      manager.saveDifferenceFilmlist();
      manager.writeHashFile();
      manager.writeIdFile();
      manager.copyFilmlist();
      manager.stop();
    }
    
  }
  void importFilmlistIntoDB() {
    manager.importFilmlist();
    FilmDBService filmDBService = new FilmDBService(manager.getExecutorService(), 2000);
    HashSet<String> allVideoUrls = filmDBService.getAllVideoUrls();
    LOG.debug("allVideoUrls loaded {} entries", allVideoUrls.size());
    manager.getFilmlist().getFilms().entrySet().parallelStream()
      .forEach(entry -> {
          if (allVideoUrls.contains(entry.getValue().getSender().name()+entry.getValue().getDefaultUrl().get().getUrl().toString())) {
            manager.getFilmlist().getFilms().remove(entry.getKey());
          }
      });
    LOG.debug("reduced to {} entries", manager.getFilmlist().getFilms().entrySet().size());
    //manager.getFilmlist().getFilms().entrySet().removeIf(entry -> filmDBService.videoExistsByUrl(entry.getValue()));
    manager.getFilmlist().getFilms().entrySet().forEach(entry -> {
        var film = entry.getValue();
        if (film.getId() == null || film.getId().isBlank()) {
            film.setId(film.getUuid().toString());
        }
    });
    LOG.debug("updated id for old films");
    manager.storeFilmsToDB();
    LOG.debug("data stored");
    manager.stop();
  }
  void checkAvailability() {
    FilmDBService filmDBService = new FilmDBService(manager.getExecutorService(), 2000);
    String condition = "WHERE last_url_check < NOW() - INTERVAL '3' DAY LIMIT 400000";
    Optional<Filmlist> dbFilmlist = filmDBService.readFilmlistFromDB(condition);
    dbFilmlist.ifPresent(filmlist -> manager.getFilmlist().addAllFilms(filmlist.getFilms().values()));
    CheckUrlAvailability checkUrlAvailability = new CheckUrlAvailability(
        manager.getConfigManager().getConfig().getCheckImportListUrlMinSize(),
        manager.getConfigManager().getConfig().getCheckImportListUrlTimeoutInSec(),
        manager.getConfigManager().getConfig().getMaximumCpuThreads());
    Filmlist abonednedList = checkUrlAvailability.getAvailableFilmlist(dbFilmlist.get(), false);
    filmDBService.deleteFilms(abonednedList.getFilms().values());
    filmDBService.update("UPDATE filme SET last_url_check = NOW() " + condition);
    manager.stop();
  }
}
