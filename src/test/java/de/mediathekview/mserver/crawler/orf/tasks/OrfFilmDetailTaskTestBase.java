package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;

import de.mediathekview.mserver.base.webaccess.JsoupConnection;
import java.util.Set;

public abstract class OrfFilmDetailTaskTestBase extends OrfTaskTestBase {

    public OrfFilmDetailTaskTestBase() {
    }

  protected Set<Film> executeTask(String aTheme, String aRequestUrl, JsoupConnection jsoupConnection) {
      return new OrfFilmDetailTask(createCrawler(), createCrawlerUrlDto(aTheme, aRequestUrl), jsoupConnection)
              .invoke();
  }
}
