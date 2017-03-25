package mServer.crawler.sender.newsearch;

import de.mediathekview.mlib.daten.DatenFilm;

public class ZdfDatenFilm extends DatenFilm {

    public ZdfDatenFilm(String ssender, String tthema, String filmWebsite, String ttitel, String uurl, String uurlRtmp,
        String datum, String zeit, long dauerSekunden, String description) {
        super(ssender, tthema, filmWebsite, ttitel, uurl, uurlRtmp, datum, zeit, dauerSekunden, description);
    }
    
    @Override
    public String getIndex() {
        // zdf uses different hosts for load balancing
        // https://rodl..., https://nrodl...
        // ignore the hosts in index to avoid duplicate entries        
        String url = getUrl();
        
        url = url.replaceFirst("https://nrodl", "https://rodl")
                .replaceFirst("http://nrodl", "http://rodl");
        
        return arr[FILM_SENDER].toLowerCase() + arr[FILM_THEMA].toLowerCase() + url;
    } 
}
