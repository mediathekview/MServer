package de.mediathekview.mserver.filmlisten.reader;

import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.base.messages.MessageCreator;
import de.mediathekview.mserver.base.messages.listener.MessageListener;
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