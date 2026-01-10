package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;

public class ArteCrawler_PL extends ArteCrawler {
  public ArteCrawler_PL(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, startPrio, Const.ARTE_PL);
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.PL;
  }

  @Override
  protected int getMaximumSubpages() {
    if (CrawlerTool.loadLongMax()) {
      return 6;
    } else {
      return 2;
    }
  }
}
