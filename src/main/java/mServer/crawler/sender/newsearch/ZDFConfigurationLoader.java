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
    private static final String FALLBACK_TOKEN_SEARCH = "309fa9bc88933de7256f4f6f6c5d3373cc36517c";
    private static final String FALLBACK_TOKEN_VIDEO = "69c4eddbe0cf82b2a9277e8106a711db314a3008";
    
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
                
                if(config.getApiToken(ZDFClient.ZDFClientMode.SEARCH).isEmpty()) {
                    Log.sysLog("Fallback token für SEARCH verwenden.");
                    config.setApiToken(ZDFClient.ZDFClientMode.SEARCH, FALLBACK_TOKEN_SEARCH);
                }
                if(config.getApiToken(ZDFClient.ZDFClientMode.VIDEO).isEmpty()) {
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
