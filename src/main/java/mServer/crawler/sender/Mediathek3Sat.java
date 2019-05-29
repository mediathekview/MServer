package mServer.crawler.sender;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.sender.newsearch.ZDFClient;
import mServer.crawler.sender.newsearch.ZDFConfigurationDTO;
import mServer.crawler.sender.newsearch.ZDFEntryDTO;

import java.util.function.Predicate;

public class Mediathek3Sat extends AbstractMediathekZdf {
  private static final String SENDERNAME = Const.DREISAT;

  public Mediathek3Sat(FilmeSuchen ssearch, int startPrio) {
    super(SENDERNAME, ssearch, startPrio);
  }

  @Override
  protected String getApiHost() {
    return "api.3sat.de";
  }

  @Override
  protected String getApiBaseUrl() {
    return "https://api.3sat.de";
  }

  @Override
  protected String getBaseUrl() {
    return "https://www.3sat.de";
  }

  @Override
  protected Predicate<? super ZDFEntryDTO> createEntryFilter() {
    return zdfEntryDTO -> zdfEntryDTO != null && zdfEntryDTO.getTvService() != null && zdfEntryDTO.getTvService().equals("3sat");
  }

  @Override
  protected ZDFConfigurationDTO loadConfig() {
    ZDFConfigurationDTO config = super.loadConfig();
    config.setApiToken(
        ZDFClient.ZDFClientMode.VIDEO, config.getApiToken(ZDFClient.ZDFClientMode.SEARCH));
    return config;
  }
}
