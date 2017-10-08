package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.IOException;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WdrUrlLoader {
    
    private final Logger LOG = LogManager.getLogger(WdrUrlLoader.class);
    
    public String executeRequest(String aUrl) {
        String result = "";

        try {
            MVHttpClient mvhttpClient = MVHttpClient.getInstance();
            OkHttpClient httpClient = mvhttpClient.getHttpClient();
            Request request = new Request.Builder()
                    .url(aUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if(response.isSuccessful()) {
                    result = response.body().string();
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
