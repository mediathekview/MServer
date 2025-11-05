package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;

public class ArteCrawler_IT extends ArteCrawler {

  public ArteCrawler_IT(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, startPrio, Const.ARTE_IT);
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.IT;
  }

}
