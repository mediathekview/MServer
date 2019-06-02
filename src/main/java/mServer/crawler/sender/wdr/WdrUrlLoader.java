package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.IOException;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WdrUrlLoader {
    
    private final Logger LOG = LogManager.getLogger(WdrUrlLoader.class);
    private static final Request.Builder requestBuilder = new Request.Builder();
    
    public String executeRequest(String aUrl) {
        String result = "";

        try {
            Request request = requestBuilder.url(aUrl).build();
            try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
                 ResponseBody body = response.body()) {
                if(response.isSuccessful() && body != null) {
                    result = body.string();
                } else {
                    LOG.error(String.format("WDR Request '%s' failed: %s", aUrl, response.code()));
                }
            }
            
        } catch (IOException ex) {
            LOG.error("Beim laden der Filme für WDR kam es zu Verbindungsproblemen.", ex);
        }

        return result;
    }    
}
