package mServer.crawler.sender;

import de.mediathekview.mlib.Const;
import mServer.crawler.FilmeSuchen;

public class MediathekDummySrfPod extends MediathekReader implements Runnable {

    public final static String SENDERNAME = Const.SRF_PODCAST;

    public MediathekDummySrfPod(FilmeSuchen ssearch, int startPrio) {
        super(ssearch, SENDERNAME,/* threads */ 2, /* urlWarten */ 200, startPrio);
    }

    @Override
    protected void addToList() {
        // empty because dummy crawler
        meldungThreadUndFertig();
    }
}
