package mServer.crawler.sender.wdr;

import de.mediathekview.mlib.Config;
import de.mediathekview.mlib.daten.ListeFilme;
import de.mediathekview.mlib.tool.Log;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class WdrLetterPageCallable implements Callable<ListeFilme> {

    private final WdrLetterPageDeserializer letterPageDeserializer = new WdrLetterPageDeserializer();
    private final String url;
    
    public WdrLetterPageCallable(String aUrl) {
        url = aUrl;
    }
    
    @Override
    public ListeFilme call() {
        ListeFilme list = new ListeFilme();

        try {
            if(!Config.getStop()) {
                Document document = Jsoup.connect(url).get();
                List<WdrSendungDto> sendungen = letterPageDeserializer.deserialize(document);
                list.addAll(parse(sendungen));
            }
        } catch(IOException ex) {
            Log.errorLog(763299002, ex);
        }
        
        return list;
    }
    
    private ListeFilme parse(List<WdrSendungDto> sendungen) {
        Collection<Future<ListeFilme>> filmList = new ArrayList<>();
        
        sendungen.forEach(sendung -> {
            ExecutorService executor = Executors.newCachedThreadPool();
            filmList.add(executor.submit(new WdrSendungCallable(sendung)));
        });            
        
        ListeFilme resultList = new ListeFilme();
        filmList.forEach(futureFilms -> {
            try {
                resultList.addAll(futureFilms.get());
            } catch (InterruptedException | ExecutionException ex) {
                Logger.getLogger(WdrLetterPageCallable.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
        return resultList;
    }
}
