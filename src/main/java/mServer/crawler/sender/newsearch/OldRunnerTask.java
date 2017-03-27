package mServer.crawler.sender.newsearch;

import java.util.Collection;
import java.util.concurrent.RecursiveTask;

import de.mediathekview.mlib.daten.DatenFilm;
import de.mediathekview.mlib.daten.ListeFilme;
import mServer.crawler.FilmeSuchen;

/**
 * Created by nicklas on 29.12.16.
 */
public class OldRunnerTask extends RecursiveTask<Collection<DatenFilm>>
{
    private static final long serialVersionUID = 1L;
    
    @Override
    protected Collection<DatenFilm> compute()
    {
        ListeFilme filmList = new ListeFilme();
        new FilmeSuchen().filmeBeimSenderLaden(filmList);
        return filmList;
    }
}
