package mServer.crawler.sender.arte;

import com.google.gson.Gson;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

/**
 * Hilfsklasse für Arte Requests
 */
public class ArteHttpClient {

  public static final String AUTH_HEADER = "Authorization";
  public static final String AUTH_TOKEN = "Bearer Nzc1Yjc1ZjJkYjk1NWFhN2I2MWEwMmRlMzAzNjI5NmU3NWU3ODg4ODJjOWMxNTMxYzEzZGRjYjg2ZGE4MmIwOA";
  public static final String USER_AGENT = "User-Agent";
  public static final String USER_AGENT_VALUE = "Mozilla/5.0";
  private static final Builder BUILDER_OPA;
  private static final Builder BUILDER;

  static {
    BUILDER_OPA = new Request.Builder().addHeader(USER_AGENT, USER_AGENT_VALUE)
            .addHeader(AUTH_HEADER, AUTH_TOKEN);

    BUILDER = new Request.Builder().addHeader(USER_AGENT, USER_AGENT_VALUE);

  }

  private static Request createRequest(String aUrl) {
    Builder b;
    if (aUrl.contains("/opa/")) {
      b = BUILDER_OPA;
    } else {
      b = BUILDER;
    }

    return b.url(aUrl).build();
  }

  public static <T> T executeRequest(Logger logger, Gson gson, String aUrl, Class<T> aDtoType) {
    T result = null;

    java.util.logging.Logger x = java.util.logging.Logger.getLogger(OkHttpClient.class.getName());
    x.setLevel(Level.FINE);

    try {
      // Wartezeit nötig wegen zu vieler paralleler Requests
      TimeUnit.MILLISECONDS.sleep(200);

      Request request = createRequest(aUrl);

      boolean stop = false;

      do {
        try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
                ResponseBody body = response.body()) {
          //response can be successful but empty...and we have to close both!
          if (response.isSuccessful() && body != null) {
            result = gson.fromJson(body.string(), aDtoType);
            stop = true;
          } else {
            if (response.code() != 429) {
              logger.error(String.format("ARTE Request '%s' failed: %s", aUrl, response.code()));
              stop = true;
            } else {
              // bei 429 (too many requests) warten und nochmal versuchen
              try {
                TimeUnit.MILLISECONDS.sleep(500);
              } catch (InterruptedException ignored) {
              }
            }
          }
        }
      } while (!stop);

    } catch (IOException | InterruptedException ex) {
      logger.error("Beim laden der Filme für Arte kam es zu Verbindungsproblemen.", ex);
      Log.errorLog(3895449, ex);
    }

    return result;
  }
}
