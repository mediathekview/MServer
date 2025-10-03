package de.mediathekview.mserver.filmlisten.reader;

import de.mediathekview.mserver.daten.Filmlist;
import de.mediathekview.mserver.base.messages.MessageCreator;
import java.io.InputStream;
import java.util.Optional;

public abstract class AbstractFilmlistReader extends MessageCreator
{
    protected AbstractFilmlistReader()
    {
        super();
    }

    public abstract Optional<Filmlist> read(InputStream aInputStream);
}