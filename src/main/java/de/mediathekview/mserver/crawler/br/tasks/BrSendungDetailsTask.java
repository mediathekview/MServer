package de.mediathekview.mserver.crawler.br.tasks;

import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mserver.crawler.br.BrCrawler;

public class BrSendungDetailsTask extends RecursiveTask<Set<Film>> {

  public BrSendungDetailsTask(final BrCrawler aBrCrawler,
      final ConcurrentLinkedQueue<String> aBrFilmIds) {
    // TODO Auto-generated constructor stub
  }

  @Override
  protected Set<Film> compute() {
    // TODO Auto-generated method stub
    return null;
  }

}
