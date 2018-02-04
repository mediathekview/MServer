package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;
import java.util.Set;

public abstract class OrfFilmDetailTaskTestBase extends OrfTaskTestBase {
  

  public OrfFilmDetailTaskTestBase() {
  }

  protected Set<Film> executeTask(String aTheme, String aRequestUrl) {
    return new OrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl)).invoke();
  }

}
