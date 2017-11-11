package mServer.crawler.sender.hr;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.Film;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.daten.Qualities;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.Callable;
import static mServer.crawler.sender.MediathekReader.urlExists;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HrSendungOverviewCallable implements Callable<ListeFilme> {

    private final HrSendungenDto dto;
    private final HrSendungOverviewDeserializer overviewDeserializer = new HrSendungOverviewDeserializer();
    private final HrSendungDeserializer sendungDeserializer = new HrSendungDeserializer();
    
    public HrSendungOverviewCallable(HrSendungenDto aDto) {
        dto = aDto;
    }
    
    @Override
    public ListeFilme call() throws Exception {
        ListeFilme list = new ListeFilme();
        try {
            if (!Config.getStop()) {
                Document overviewDocument = Jsoup.connect(dto.getUrl()).get();
                List<String> detailUrls = overviewDeserializer.deserialize(overviewDocument);
                
                detailUrls.forEach(detailUrl -> {
                    
                    if (!Config.getStop()) {
                        Film film = handleFilmDetails(detailUrl);
                        
                        if(film != null) {
                            list.add(film);
                        }
                    }
                });
            }
        } catch (IOException ex1) {
            Log.errorLog(894651554, ex1);
        }
        return list;
    }
    
    private Film handleFilmDetails(String url) {
        try {
            Document detailDocument = Jsoup.connect(url).get();
            Film film = sendungDeserializer.deserialize(dto.getTheme(), url, detailDocument);

            if (film != null) {
                
                String filmUrl = film.getUrl(Qualities.NORMAL).toString();
                String subtitle = filmUrl.replace(".mp4", ".xml");
                
                if (urlExists(subtitle)) {
                    try {
                        film.addSubtitle(new URI(subtitle));
                    } catch (URISyntaxException ex) {
                        Log.errorLog(894561212, ex);
                    }
                }
                return film;
            }
        } catch (IOException | URISyntaxException ex1) {
            Log.errorLog(894651554, ex1);
        }
        
        return null;
    }
}
