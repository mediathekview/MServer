package mServer.crawler.sender.newsearch;

import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * A simple singleton to read the ZDF configuration just once per runtime.
 */
public class ZDFConfigurationLoader {

  public static final String ZDF_BEARER_URL = "https://www.zdf.de/";
  private static final String FALLBACK_TOKEN_SEARCH = "5bb200097db507149612d7d983131d06c79706d5";
  private static final String FALLBACK_TOKEN_VIDEO = "20c238b5345eb428d01ae5c748c5076f033dfcc7";

  private static ZDFConfigurationLoader instance;

  private ZDFConfigurationDTO config;

  private ZDFConfigurationLoader() {
    config = null;
  }

  public static ZDFConfigurationLoader getInstance() {
    if (instance == null) {
      instance = new ZDFConfigurationLoader();
    }
    return instance;
  }

  public ZDFConfigurationDTO loadConfig() {
    if (config == null) {

      Document document;
      try {
        document = Jsoup.connect(ZDF_BEARER_URL).get();
        ZdfIndexPageDeserializer deserializer = new ZdfIndexPageDeserializer();
        config = deserializer.deserialize(document);

        if (config.getApiToken(ZDFClient.ZDFClientMode.SEARCH).isEmpty()) {
          Log.sysLog("Fallback token für SEARCH verwenden.");
          config.setApiToken(ZDFClient.ZDFClientMode.SEARCH, FALLBACK_TOKEN_SEARCH);
        }
        if (config.getApiToken(ZDFClient.ZDFClientMode.VIDEO).isEmpty()) {
          Log.sysLog("Fallback token für VIDEO verwenden.");
          config.setApiToken(ZDFClient.ZDFClientMode.VIDEO, FALLBACK_TOKEN_VIDEO);
        }

      } catch (IOException ex) {
        Log.errorLog(561515615, ex);

        config = new ZDFConfigurationDTO();
        config.setApiToken(ZDFClient.ZDFClientMode.SEARCH, FALLBACK_TOKEN_SEARCH);
        config.setApiToken(ZDFClient.ZDFClientMode.VIDEO, FALLBACK_TOKEN_VIDEO);
      }
    }
    return config;
  }
}
