package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import okhttp3.Call;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import org.apache.logging.log4j.Logger;

/**
 * Hilfsklasse für Arte Requests
 */
public class ArteHttpClient {
    public static final String AUTH_HEADER = "Authorization";
    public static final String AUTH_TOKEN = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
    public static final String USER_AGENT = "User-Agent";
    public static final String USER_AGENT_VALUE = "Mozilla/5.0";

    public static <T extends Object> T executeRequest(Logger logger, Gson gson, String aUrl, Class<T> aDtoType) {
        T result = null;

        java.util.logging.Logger x = java.util.logging.Logger.getLogger(OkHttpClient.class.getName());
        x.setLevel(Level.FINE);
        
        try {
          // Wartezeit nötig wegen zu vieler paralleler Requests
          TimeUnit.MILLISECONDS.sleep(200);
          
            MVHttpClient mvhttpClient = MVHttpClient.getInstance();
            OkHttpClient httpClient = mvhttpClient.getHttpClient();
            Request request = createRequest(aUrl);
            
            boolean stop = false;
            
            do {
              Call call = httpClient.newCall(request);
              try (Response response = call.execute()) {
                  if(response.isSuccessful()) {
                      result = gson.fromJson(response.body().string(), aDtoType);
                      stop = true;
                  } else {
                      if(response.code() != 429) {
                        logger.error(String.format("ARTE Request '%s' failed: %s", aUrl, response.code()));
                        stop = true;
                      } else {
                        // bei 429 (too many requests) warten und nochmal versuchen
                        try {
                          TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException ex) {
                        }
                      }
                  }
              }
            } while(!stop);
            
        } catch (IOException | InterruptedException ex) {
            logger.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.", ex);
        }

        return result;
    }
    
    private static Request createRequest(String aUrl) {
      Builder builder = new Request.Builder();
      
      // nur bei opa-Anfragen muss eine Authentifzierung erfolgen
      if (aUrl.contains("/opa/")) {
        builder = builder.addHeader(AUTH_HEADER, AUTH_TOKEN);
      }

      return builder.addHeader(USER_AGENT, USER_AGENT_VALUE)
        .url(aUrl).build();      
    }
}
