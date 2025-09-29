package de.mediathekview.mlib.filmlisten.reader;

import de.mediathekview.mlib.daten.Filmlist;
import de.mediathekview.mlib.messages.MessageCreator;
import de.mediathekview.mlib.messages.listener.MessageListener;
import java.io.InputStream;
import java.util.Optional;

public abstract class AbstractFilmlistReader extends MessageCreator
{
    protected AbstractFilmlistReader()
    {
        super();
    }

    protected AbstractFilmlistReader(final MessageListener... aListeners)
    {
        super(aListeners);
    }

    public abstract Optional<Filmlist> read(InputStream aInputStream);
}