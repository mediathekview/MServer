package mServer.crawler.sender.hr;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.Const;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import de.mediathekview.mlib.tool.MVHttpClient;
import mServer.crawler.CrawlerTool;
import mServer.crawler.FilmeSuchen;
import mServer.crawler.RunSender;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.concurrent.Callable;

import static mServer.crawler.sender.MediathekReader.urlExists;

public class HrSendungOverviewCallable implements Callable<ListeFilme> {

  private final HrSendungenDto dto;
  private final HrSendungOverviewDeserializer overviewDeserializer = new HrSendungOverviewDeserializer();
  private final HrSendungDeserializer sendungDeserializer = new HrSendungDeserializer();
  private static final Logger LOG = LogManager.getLogger();

  public HrSendungOverviewCallable(HrSendungenDto aDto) {
    dto = aDto;
  }

  @Override
  public ListeFilme call() throws Exception {
    ListeFilme list = new ListeFilme();
    try {
      if (!Config.getStop()) {
        FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.ANZAHL);

        Request request = new Request.Builder().url(dto.getUrl()).get().build();
        try (Response resp = MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute();
             ResponseBody body = resp.body()) {
          if (resp.isSuccessful() && body != null) {
            Document overviewDocument = Jsoup.parse(body.string());
            List<String> detailUrls = overviewDeserializer.deserialize(overviewDocument);

            detailUrls.forEach(detailUrl -> {

              if (!Config.getStop()) {
                DatenFilm film = handleFilmDetails(detailUrl);

                if (film != null) {
                  list.add(film);
                }
              }
            });
          }
          else
            LOG.error("Http request was either unsuccessful or body was null, success = {}", resp.isSuccessful());
        }
      }
    }
    catch (SocketTimeoutException ex) {
      LOG.error("Timeout occured for URL={}", dto.getUrl(),ex);
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLVERSUCHE);
    }
    catch (IOException ex1) {
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLVERSUCHE);
      Log.errorLog(894651554, ex1);
    }
    return list;
  }

  private DatenFilm handleFilmDetails(String url) {
    FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.ANZAHL);
    Request request = new Request.Builder().url(url).get().build();
    try (Response resp = MVHttpClient.getInstance().getReducedTimeOutClient().newCall(request).execute();
         ResponseBody body = resp.body()) {
      if (resp.isSuccessful() && body != null) {
        Document detailDocument = Jsoup.parse(body.string());
        DatenFilm film = sendungDeserializer.deserialize(dto.getTheme(), url, detailDocument);

        if (film != null) {
          String subtitle = film.getUrl().replace(".mp4", ".xml");

          if (urlExists(subtitle)) {
            CrawlerTool.addUrlSubtitle(film, subtitle);
          }
          return film;
        }
      }
      else
        LOG.error("Http request was either unsuccessful or body was null, success = {}", resp.isSuccessful());

    }
    catch (SocketTimeoutException ex) {
      LOG.error("Timeout occured for URL={}", url,ex);
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLVERSUCHE);
    }
    catch (IOException ex1) {
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLER);
      FilmeSuchen.listeSenderLaufen.inc(Const.HR, RunSender.Count.FEHLVERSUCHE);
      Log.errorLog(894651554, ex1);
    }

    return null;
  }
}
