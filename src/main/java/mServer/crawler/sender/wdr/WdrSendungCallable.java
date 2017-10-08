package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WdrSendungCallable implements Callable<ListeFilme> {

    private final WdrSendungDto dto;
    private final WdrSendungOverviewDeserializer overviewDeserializer = new WdrSendungOverviewDeserializer();

    public WdrSendungCallable(WdrSendungDto aDto) {
        dto = aDto;
    }
    
    @Override
    public ListeFilme call() {
        
        return parse(dto, 0);
    }
    
    private ListeFilme parse(WdrSendungDto aDto, int recoursiveCall) {
        ListeFilme list = new ListeFilme();
        
        if (!Config.getStop()) {
            aDto.getOverviewUrls().forEach(url -> {
                if(isUrlRelevant(url) && recoursiveCall < 2) {
                    list.addAll(parseSendungOverviewPage(url, aDto.getTheme(), recoursiveCall+1));
                }
            });

            aDto.getVideoUrls().forEach(url -> {
                if(isUrlRelevant(url)) {
                    DatenFilm film = parseFilmPage(url, aDto.getTheme());
                    if(film != null) {
                        list.add(film);
                    }
                }
            });
        }     
        
        return list;
    }
    
    private Collection<DatenFilm> parseSendungOverviewPage(String strUrl, String parentTheme, int recoursiveCall) {
        
        if(!isUrlRelevant(strUrl)) {
            return new ArrayList<>();
        }

        try {
            Document filmDocument = Jsoup.connect(strUrl).get();
            WdrSendungDto sendungDto = overviewDeserializer.deserialize(filmDocument);
            sendungDto.setTheme(parentTheme);

            return parse(sendungDto, recoursiveCall);
        } catch(IOException ex) {
            Log.errorLog(763299001, ex);
        }

        return new ArrayList<>();
    }

        /***
         * Filtert URLs heraus, die nicht durchsucht werden sollen
         * Hintergrund: diese URLs verweisen auf andere und führen bei der Suche
         * im Rahmen der Rekursion zu endlosen Suchen
         * @param url zu prüfende URL
         * @return true, wenn die URL verarbeitet werden soll, sonst false
         */
        private boolean isUrlRelevant(String url) {
            // die Indexseite der Lokalzeit herausfiltern, da alle Beiträge
            // um die Lokalzeitenseiten der entsprechenden Regionen gefunden werden
            if(url.endsWith("lokalzeit/index.html")) {
                return false;
            } else if(url.contains("wdr.de/hilfe")) {
                return false;
            }
            
            return true;
        }

        private DatenFilm parseFilmPage(String filmWebsite, String theme) {
            
            if(Config.getStop()) {
                return null;
            }
            
            try {
                Document filmDocument = Jsoup.connect(filmWebsite).get();
                WdrVideoDetailsDeserializer deserializer = new WdrVideoDetailsDeserializer(new WdrUrlLoader());
                DatenFilm film = deserializer.deserialize(theme, filmDocument);

                return film;
            } catch(IOException ex) {
                Log.errorLog(763299001, ex);
            }
            
            return null;
        }
    
}
