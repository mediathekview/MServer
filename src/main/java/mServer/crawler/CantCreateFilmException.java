package mServer.crawler;

import de.mediathekview.mlib.daten.Film;

/**
 * A exception to handle problems while film creating.
 */
public class CantCreateFilmException extends Exception
{

    private static final String FILM_ERROR_TEXT_PATTERN = "Der Film \"%s-%s\" konnte nicht erzeugt weden.";
    private static final String FILM_ERROR_TEXT_SIMPLE = "Ein Film konnte nicht erzeugt weden.";

    public CantCreateFilmException(Exception aExecption)
    {
        super(FILM_ERROR_TEXT_SIMPLE,aExecption);
    }

    public CantCreateFilmException(Film aFilm)
    {
        super(String.format(FILM_ERROR_TEXT_PATTERN,aFilm.getThema(),aFilm.getTitel()));
    }

    public CantCreateFilmException(Exception aExecption, Film aFilm)
    {
        super(String.format(FILM_ERROR_TEXT_PATTERN,aFilm.getThema(),aFilm.getTitel()),aExecption);
    }
}
