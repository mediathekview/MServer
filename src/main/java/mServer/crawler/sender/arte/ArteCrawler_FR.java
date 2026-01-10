package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;

public class ArteCrawler_FR extends ArteCrawler {

  public ArteCrawler_FR(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, startPrio, Const.ARTE_FR);
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.FR;
  }


  @Override
  protected int getMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return 10;
    } else {
      return 4;
    }
  }
}
