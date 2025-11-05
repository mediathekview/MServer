package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;

public class ArteCrawler_ES extends ArteCrawler {

  public ArteCrawler_ES(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, startPrio, Const.ARTE_ES);
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.ES;
  }

}
