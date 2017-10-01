package mServer.crawler.sender.hr;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import mServer.crawler.CrawlerTool;
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
                        DatenFilm film = handleFilmDetails(detailUrl);
                        
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
    
    private DatenFilm handleFilmDetails(String url) {
        try {
            Document detailDocument = Jsoup.connect(url).get();
            DatenFilm film = sendungDeserializer.deserialize(dto.getTheme(), url, detailDocument);

            if (film != null) {
                String subtitle = film.getUrl().replace(".mp4", ".xml");
                
                if (urlExists(subtitle)) {
                    CrawlerTool.addUrlSubtitle(film, subtitle);
                }
                return film;
            }
        } catch (IOException ex1) {
            Log.errorLog(894651554, ex1);
        }        
        
        return null;
    }
}
