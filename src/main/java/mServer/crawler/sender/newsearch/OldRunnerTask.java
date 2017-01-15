package mServer.crawler.sender.newsearch;

import mSearch.daten.DatenFilm;
import mSearch.daten.ListeFilme;
import mServer.crawler.FilmeSuchen;

import java.util.Collection;
import java.util.concurrent.RecursiveTask;

/**
 * Created by nicklas on 29.12.16.
 */
@SuppressWarnings("serial")
public class OldRunnerTask extends RecursiveTask<Collection<DatenFilm>>
{

    @Override
    protected Collection<DatenFilm> compute()
    {
        ListeFilme filmList = new ListeFilme();
        new FilmeSuchen().filmeBeimSenderLaden(filmList);
        return filmList;
    }
}
