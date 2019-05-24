package mServer.crawler.sender.newsearch;

import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/** A simple singleton to read the ZDF configuration just once per runtime. */
public class ZDFConfigurationLoader {

  private static final String FALLBACK_TOKEN_SEARCH = "5bb200097db507149612d7d983131d06c79706d5";
  private static final String FALLBACK_TOKEN_VIDEO = "20c238b5345eb428d01ae5c748c5076f033dfcc7";

  private final String baseUrl;

  private ZDFConfigurationDTO config;

  public ZDFConfigurationLoader(String aBaseUrl) {
    baseUrl = aBaseUrl;
    config = null;
  }

  public ZDFConfigurationDTO loadConfig() {
    if (config == null) {

      Document document;
      try {
        document = Jsoup.connect(baseUrl).get();
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
