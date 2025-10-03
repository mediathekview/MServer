package de.mediathekview.mserver.base.messages;

public enum LibMessages implements Message
{
    FILMLIST_WRITE_ERROR("filmlistWriteError", MessageTypes.FATAL_ERROR),
    FILMLIST_COMPRESS_ERROR("filmlistCompressError", MessageTypes.FATAL_ERROR),
    FILMLIST_DECOMPRESS_ERROR("filmlistDecompressError", MessageTypes.FATAL_ERROR),
    FILMLIST_WRITE_STARTED("filmlistWriteStarted", MessageTypes.INFO),
    FILMLIST_WRITE_FINISHED("filmlistWriteFinished", MessageTypes.INFO),
    FILMLIST_READ_ERROR("filmlistReadError", MessageTypes.FATAL_ERROR),
    FILMLIST_IMPORT_STARTED("filmlistImportStarted", MessageTypes.INFO),
    FILMLIST_IMPORT_FINISHED("filmlistImportFinished", MessageTypes.INFO);

    private String messageKey;
    private MessageTypes messageType;

    LibMessages(final String aMessageKey, final MessageTypes aMessageType)
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