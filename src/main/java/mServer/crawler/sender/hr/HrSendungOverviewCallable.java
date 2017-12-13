package mServer.crawler.sender.hr;

import static mServer.crawler.sender.MediathekReader.urlExists;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Callable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Resolution;
import de.mediathekview.mlib.tool.Log;

public class HrSendungOverviewCallable implements Callable<ListeFilme> {

  private final HrSendungenDto dto;
  private final HrSendungOverviewDeserializer overviewDeserializer =
      new HrSendungOverviewDeserializer();
  private final HrSendungDeserializer sendungDeserializer = new HrSendungDeserializer();

  public HrSendungOverviewCallable(final HrSendungenDto aDto) {
    dto = aDto;
  }

  @Override
  public ListeFilme call() throws Exception {
    final ListeFilme list = new ListeFilme();
    try {
      if (!Config.getStop()) {
        final Document overviewDocument = Jsoup.connect(dto.getUrl()).get();
        final List<String> detailUrls = overviewDeserializer.deserialize(overviewDocument);

        detailUrls.forEach(detailUrl -> {

          if (!Config.getStop()) {
            final Film film = handleFilmDetails(detailUrl);

            if (film != null) {
              list.add(film);
            }
          }
        });
      }
    } catch (final IOException ex1) {
      Log.errorLog(894651554, ex1);
    }
    return list;
  }

  private Film handleFilmDetails(final String url) {
    try {
      final Document detailDocument = Jsoup.connect(url).get();
      final Film film = sendungDeserializer.deserialize(dto.getTheme(), url, detailDocument);

      if (film != null) {

        final String filmUrl = film.getUrl(Resolution.NORMAL).toString();
        final String subtitle = filmUrl.replace(".mp4", ".xml");

        if (urlExists(subtitle)) {
          try {
            film.addSubtitle(new URL(subtitle));
          } catch (final MalformedURLException ex) {
            Log.errorLog(894561212, ex);
          }
        }
        return film;
      }
    } catch (final IOException ex1) {
      Log.errorLog(894651554, ex1);
    }

    return null;
  }
}
