package de.mediathekview.mserver.filmlisten;

import de.mediathekview.mserver.base.messages.Message;
import de.mediathekview.mserver.base.messages.MessageTypes;

public enum FilmListMessages implements Message
{
    FILMLIST_WRITE_ERROR("filmlistWriteError", MessageTypes.FATAL_ERROR),
    FILMLIST_COMPRESS_ERROR("filmlistCompressError", MessageTypes.FATAL_ERROR),
    FILMLIST_DECOMPRESS_ERROR("filmlistDecompressError", MessageTypes.FATAL_ERROR),
    FILMLIST_WRITE_STARTED("filmlistWriteStarted", MessageTypes.INFO),
    FILMLIST_WRITE_FINISHED("filmlistWriteFinished", MessageTypes.INFO),
    FILMLIST_READ_ERROR("filmlistReadError", MessageTypes.FATAL_ERROR),
    FILMLIST_IMPORT_STARTED("filmlistImportStarted", MessageTypes.INFO),
    FILMLIST_IMPORT_FINISHED("filmlistImportFinished", MessageTypes.INFO);

    private final String messageKey;
    private final MessageTypes messageType;

    FilmListMessages(final String aMessageKey, final MessageTypes aMessageType)
    {
        messageKey = aMessageKey;
        messageType = aMessageType;
    }

    @Override
    public String getMessageKey()
    {
        return messageKey;
    }

    @Override
    public MessageTypes getMessageType()
    {
        return messageType;
    }
}