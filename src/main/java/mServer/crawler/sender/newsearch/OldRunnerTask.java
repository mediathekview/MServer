package mServer.crawler.sender.newsearch;

import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mServer.crawler.FilmeSuchen;

import java.util.Collection;
import java.util.concurrent.RecursiveTask;

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
