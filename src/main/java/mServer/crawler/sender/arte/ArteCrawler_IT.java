package mServer.crawler.sender.arte;

import de.mediathekview.mlib.Const;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;

public class ArteCrawler_IT extends ArteCrawler {

  public ArteCrawler_IT(FilmeSuchen ssearch, int startPrio) {
    super(ssearch, startPrio, Const.ARTE_IT);
  }

  @Override
  protected ArteLanguage getLanguage() {
    return ArteLanguage.IT;
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
