package de.mediathekview.mserver.crawler.orf.tasks;

import de.mediathekview.mlib.daten.Film;

import de.mediathekview.mserver.crawler.orf.OrfCrawler;

import java.util.Set;

public abstract class OrfFilmDetailTaskTestBase extends OrfTaskTestBase {

    public OrfFilmDetailTaskTestBase() {
    }

  protected Set<Film> executeTask(OrfCrawler crawler, String aTheme, String aRequestUrl) {
      return new OrfFilmDetailTask(crawler, createCrawlerUrlDto(aTheme, aRequestUrl), false)
              .invoke();
  }
}
