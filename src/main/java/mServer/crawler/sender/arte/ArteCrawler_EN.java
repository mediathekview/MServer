package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;

public class ArteCrawler_EN extends ArteCrawler {

  public ArteCrawler_EN(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, startPrio, Const.ARTE_EN);
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.EN;
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
