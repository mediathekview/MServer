package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.IOException;
import java.util.logging.Level;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;

/**
 * Hilfsklasse für Arte Requests
 */
public class ArteHttpClient {
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_TOKEN = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";

    public static <T extends Object> T executeRequest(Logger logger, Gson gson, String aUrl, Class<T> aDtoType) {
        T result = null;

        java.util.logging.Logger x = java.util.logging.Logger.getLogger(OkHttpClient.class.getName());
        x.setLevel(Level.FINE);
        
        try {
            MVHttpClient mvhttpClient = MVHttpClient.getInstance();
            OkHttpClient httpClient = mvhttpClient.getHttpClient();
            Request request = new Request.Builder()
                    .addHeader(AUTH_HEADER, AUTH_TOKEN)
                    .url(aUrl).build();
            try (Response response = httpClient.newCall(request).execute()) {
                if(response.isSuccessful()) {
                    result = gson.fromJson(response.body().string(), aDtoType);
                } else {
                    logger.error(String.format("ARTE Request '%s' failed: %s", aUrl, response.code()));
                }
            }
            
        } catch (IOException ex) {
            logger.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.", ex);
        }

        return result;
    }
}
