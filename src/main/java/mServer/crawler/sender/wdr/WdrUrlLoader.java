package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.tool.MVHttpClient;
import java.io.IOException;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WdrUrlLoader {

  private final Logger LOG = LogManager.getLogger(WdrUrlLoader.class);

  public String executeRequest(String aUrl) {
    String result = "";

    try {
      Request request = new Request.Builder()
              .url(aUrl).build();
      try (Response response = MVHttpClient.getInstance().getHttpClient().newCall(request).execute();
              ResponseBody body = response.body()) {
        FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.ANZAHL);
        if (response.isSuccessful() && body != null) {
          result = body.string();
          FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.SUM_DATA_BYTE, result.length());
          FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.SUM_TRAFFIC_BYTE, result.length());
        } else {
          FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.FEHLER);
          FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.FEHLVERSUCHE);
          LOG.error(String.format("WDR Request '%s' failed: %s", aUrl, response.code()));
        }
      }

    } catch (IOException ex) {
      FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(Const.WDR, RunSender.Count.FEHLVERSUCHE);
      LOG.error("Beim laden der Filme für WDR kam es zu Verbindungsproblemen.", ex);
    }

    return result;
  }
}
