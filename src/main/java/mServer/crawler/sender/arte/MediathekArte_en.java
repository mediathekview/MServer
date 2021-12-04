package mServer.crawler.sender.arte;

import mServer.crawler.FilmeSuchen;

public class MediathekArte_en extends MediathekArte_de {

    protected static final String SENDERNAME = "ARTE.EN";

    public MediathekArte_en(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, startPrio, SENDERNAME);
        LANG_CODE = "en";
    }
}
