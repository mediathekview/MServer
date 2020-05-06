package mServer.crawler.sender.dreisat;

import de.mediathekview.mlib.Const;
import org.jetbrains.annotations.NotNull;
import java.util.concurrent.ExecutionException;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.zdf.AbstractZdfCrawler;
import mServer.crawler.sender.zdf.ZdfConfiguration;

public class DreiSatCrawler extends AbstractZdfCrawler {

  public DreiSatCrawler(FilmeSuchen ssearch, int startPrio) {
    super(Const.DREISAT, ssearch, startPrio);
  }

  /**
   * Loads the api auth token. And because 3Sat only uses one token for search
   * and videos the search auth key will be set as video key too.
   *
   * @return The configuration containing the auth key.
   * @throws ExecutionException   Could be thrown if something went's wrong while
   *                              searching.
   * @throws InterruptedException Could be thrown if the task will be
   *                              interrupted.
   */
  @Override
  protected ZdfConfiguration loadConfiguration() throws ExecutionException, InterruptedException {
    final ZdfConfiguration config = super.loadConfiguration();
    config.getSearchAuthKey().ifPresent(config::setVideoAuthKey);
    return config;
  }

  @Override
  protected @NotNull
  String getUrlBase() {
    return DreisatConstants.URL_BASE;
  }

  @Override
  protected String getApiUrlBase() {
    return DreisatConstants.URL_API_BASE;
  }

  @Override
  protected @NotNull
  String getUrlDay() {
    return DreisatConstants.URL_DAY;
  }
}
